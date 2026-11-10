import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

public class renderer{
    public static void main(String[] args) throws Exception{
        MeshLoader M = new MeshLoader();
        float[] base_mesh = M.load_default_mesh(2.0f);
        int[] uv = M.load_default_uv();
        int[][] texture = M.load_default_texture();
        int[] color = new int[base_mesh.length/9];
        for(int i = 0 ; i < base_mesh.length/9 ; i++){
            color[i] = 0xFFFFFFFF;//(int)(Math.random()*35514566);
        }
        float fov = 90;
        float zoom = 5;
        int[] mesh_index = {0, base_mesh.length};

        render r = new render(mesh_index[mesh_index.length - 1], 1000, 1000, fov, zoom, -12f, 0.0f, 7f, 2.25f, 0.0f, texture);
        
        viewer v = new viewer(r.pixel_x, r.pixel_y, 1, "render");

        v.r = r;
        r.LX = -10f;
        r.LY = 10f;
        float[] basemesh_clone = base_mesh.clone();
        r.camedit(fov,zoom, -2.1f*5f, 0.0f, 1.225f*5f, 2.17f, 2.087f, 0.0f);
        for (int f = 0;  ; f++) {
                basemesh_clone = base_mesh.clone();
                r.editmesh(basemesh_clone, basemesh_clone,mesh_index[0], mesh_index[1], (float)Math.toRadians(f), 0, 0, 0, 0, 0.3f);
                
                
                
                v.show(r.project(basemesh_clone, uv));
        }
    }
}

class render extends Thread{
    int pixel_x; // screen resolution for now hard coded to 1000x1000 because of the hardcoded pixel distribution for the multithreaded shading and rasterization
    int pixel_y; // screen resolution for now hard coded to 1000x1000 because of the hardcoded pixel distribution for the multithreaded shading and rasterization
    int tri_count; // total number of triangle in the 3d scene
    int[] triangle_index; //index of triangle for a pixel which is on top of every oother triangle : used for fast shading
    float[] zbuffer; // distance from the camera to the triangle which is being rasterised : its defined for each pixel used for shading as well
    float scaling = pixel_y/2; // used for converting ndc plane points into screen pixel points
    float[] data; //its the data that rasterizer uses to create the zbuffer
    boolean[] tri_isvisible;
    float alpha; // distance from a particular and camera point used for dolly zoom
    float zoom; // zooming factor, liner multipication to make a object appear closer
    int[] image; //rgb value of screen stored in a 1d int array
    float view_fac; //multiplication factor used for lowering the number of calculation
    int rejection = 0; //debug / performance measuring index
    int[] last_tri_index = new int[16]; // number of triangle which are sent to each tile while rasterization
    float cam_x; float cam_y; float cam_z; // camera position in 3d space
    int[] chunk_pixel_index = {0, 250, 500, 750, 1000}; // unused
    float[] triangle; //saves the triangle data after normalization of 3d world
    float polar_angle; float azimuthal_angle; // camera pointing angles
    int runx;
    int runy;
    int fontbitx = 5;
    int fontbity = 7;
    int[] processed_image;
    int brightness = 128;

    multithreadrender mtr; // multithreader object

    int scanline = 4; // out of 4 useful for crt effect

    int[][] tri_chunk_index; //it stored index of triangle for each tile

    boolean ASCIIeffect = false;

    public render(int pointcount , int resox, int resoy, float fov, float zoom, float cam_x, float cam_y ,float cam_z, float polar_angle,float azimuthal_angle, int[][] texture){
        tri_count = pointcount/9;
        triangle = new float[pointcount];
        pixel_x = resox;
        pixel_y = resoy;
        triangle_index = new int[resox * resoy];
        image = new int[resox * resoy];
        zbuffer = new float[resox*resoy];
        data = new float[tri_count*24];
        tri_isvisible = new boolean[tri_count];
        scaling = (resoy/2);
        alpha = (float)(1/(Math.tan(Math.toRadians(fov/2))));
        this.zoom = zoom;
        view_fac = zoom*alpha;
        this.cam_x = cam_x;
        this.cam_y = cam_y;
        this.cam_z = cam_z;
        this.azimuthal_angle = azimuthal_angle;
        this.polar_angle = polar_angle;
        tri_chunk_index = new int[16][tri_count];
        System.out.println(view_fac);
        mtr = new multithreadrender(this, pixel_x, pixel_y, texture);
        processed_image = new int[pixel_x*pixel_y];
        runx = resox / (fontbitx);
        runy = resoy / (fontbity);
    }
    static final boolean[][][] FONT_BOOL = {
        {{false, false, false, false, false}, {false, false, false, false, false}, {false, false, false, false, false}, {false, false, true, false, false}, {false, false, false, false, false}, {false, false, false, false, false}, {false, false, true, false, false}}, // ':'
        {{false, false, false, false, false}, {false, false, false, false, false}, {false, false, false, false, false}, {false, false, false, false, false}, {false, false, false, false, false}, {false, true, true, false, false}, {false, false, true, false, false}}, // ','
        {{false, false, false, false, false}, {false, false, true, false, false}, {false, false, true, false, false}, {false, false, true, false, false}, {false, false, true, false, false}, {false, false, true, false, false}, {false, false, false, false, false}}, // '!'--
        {{false, false, false, false, false}, {false, true, false, true, false}, {false, true, false, true, false}, {false, true, false, true, false}, {false, false, false, false, false}, {false, false, false, false, false}, {false, false, false, false, false}}, // '"'
        {{false, false, false, false, false}, {false, false, true, false, false}, {false, true, true, true, false}, {false, false, true, false, false}, {false, true, false, true, false}, {false, false, false, false, false}, {false, false, false, false, false}}, // '*'
        {{false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, false}, {true, false, false, false, false}, {true, false, false, false, false}, {true, false, false, false, false}, {true, false, false, false, false}}, // '('
        {{false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, true}, {false, false, false, false, true}, {false, false, true, true, false}, {false, false, true, false, false}, {false, false, false, false, false}}, // '?'
        {{false, false, false, false, false}, {false, false, false, false, false}, {false, false, false, false, false}, {true, false, false, false, true}, {true, false, false, false, true}, {true, false, true, false, true}, {true, false, true, false, true}}, // 'w'
        {{false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, true}, {false, false, false, false, true}, {false, true, true, true, false}, {true, false, false, false, false}, {true, false, false, false, false}}, // '2'
        {{false, false, false, false, false}, {true, true, true, true, true}, {true, false, false, false, false}, {true, false, false, false, false}, {true, true, true, false, false}, {true, false, false, false, false}, {true, false, false, false, false}}, // 'E'
        {{false, false, false, false, false}, {true, false, false, true, false}, {true, false, false, true, false}, {true, false, false, true, false}, {true, true, true, true, true}, {false, false, false, true, false}, {false, false, false, true, false}}, // '4'
        {{false, false, false, false, false}, {false, true, true, false, false}, {true, false, false, true, false}, {true, false, false, true, false}, {false, true, true, true, true}, {true, false, false, true, false}, {true, false, false, true, false}}, // '&'
        {{false, false, false, false, false}, {false, true, true, true, false}, {true, false, false, false, true}, {false, false, true, true, true}, {false, true, false, false, true}, {false, false, true, true, true}, {true, false, false, false, true}}, // '@'
        {{false, false, false, false, false}, {false, false, true, false, false}, {false, true, true, true, true}, {true, false, true, false, false}, {false, true, true, true, false}, {false, false, true, false, true}, {true, true, true, true, false}}, // '$'
        {{false, false, false, false, false}, {false, true, true, false, true}, {true, false, true, false, true}, {true, true, true, true, false}, {false, false, true, false, false}, {false, true, false, true, true}, {true, false, true, false, true}}, // '%'
        {{false, false, false, false, false}, {false, true, false, true, false}, {true, true, true, true, true}, {false, true, false, true, false}, {false, true, false, true, false}, {true, true, true, true, true}, {false, true, false, true, false}}, // '#'
    };
    
    float LX = -10; float LY = -10; float LZ = 10; float intensity = 1000; // lighing position

    float w1; float w2; float w3; 
    float x ; float y ; float z ; 
    float PX; float PY; float PZ; 

    float x1; float y1; float z1; 
    float x2; float y2; float z2; 
    float x3; float y3; float z3; 

    float X1; float Y1; float Z1; 
    float X2; float Y2; float Z2; 
    float X3; float Y3; float Z3; 

    int x_min;
    int x_max;
    int y_min;
    int y_max;

    float rotation  = 0.0f;
    float rotation1 = 0.0f;
    float rotation2 = 0.0f;
    float denom;
    float comz;
    float compens = 0;

    //   CAMERA VARIABLES ARE EDITED HERE ON RUN TIME
    void camedit(float fov, float zoom, float cam_x, float cam_y ,float cam_z, float d0 , float polar_angle,float azimuthal_angle){
        alpha = (float)(1/(Math.tan(Math.toRadians(fov/2))));
        this.zoom = zoom;
        view_fac = zoom*alpha;
        compens = d0*(alpha -1);
        this.azimuthal_angle = azimuthal_angle;
        this.polar_angle = polar_angle;
        this.cam_x = cam_x - compens*(float)(Math.sin(polar_angle)*Math.cos(azimuthal_angle));
        this.cam_y = cam_y - compens*(float)(Math.sin(polar_angle)*Math.sin(azimuthal_angle));
        this.cam_z = cam_z - compens*(float)(Math.cos(polar_angle));
    }

    //    BASE MESH IS EITHER DISPLACED OR ROTATED AND STORED INTO A CLONE, BASE MESH CAN BE SPILLED INTO A SECTION FOR FLEXIBLE CHANGE OF INDIVIDUAL OBJECTS
    void editmesh(float[] tri, float[] meshout, int a, int b, float rotation, float phi, float theta, float displace_x, float displace_y, float displace_z){
        float w =   (float)(Math.cos(rotation/2));
        float u_x = (float)(Math.sin(rotation/2)*Math.sin(theta)*Math.cos(phi));
        float u_y = (float)(Math.sin(rotation/2)*Math.sin(theta)*Math.sin(phi));
        float u_z = (float)(Math.sin(rotation/2)*Math.cos(theta));
        
        for(int i = a/3 ; i< b/3 ; i++){
            x = tri[3*i];
            y = tri[3*i + 1];
            z = tri[3*i + 2];
            float tx = x*(1-2*(u_y*u_y + u_z*u_z)) + 2*y*(u_x*u_y - w*u_z) + 2*z*(u_x*u_z + w*u_y) + displace_x;
            float ty = 2*x*(u_x*u_y + w*u_z) + y*(1-2*(u_x*u_x + u_z*u_z)) + 2*z*(u_y*u_z - w*u_x) + displace_y;
            float tz = 2*x*(u_x*u_z - w*u_y) + 2*y*(u_y*u_z + w*u_x) + z*(1-2*(u_x*u_x + u_y*u_y)) + displace_z;
            meshout[3*i] =     tx;
            meshout[3*i + 1] = ty;
            meshout[3*i + 2] = tz;
        }
    }
    // THREADPOOL CREATION FOR MULTITHREADED METHOD CALLING FOR RASTERIZATION AND SHADIND
    ThreadPoolExecutor run1 = new ThreadPoolExecutor(4 ,  4, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(2000), new ThreadPoolExecutor.CallerRunsPolicy());
    ThreadPoolExecutor run2 = new ThreadPoolExecutor(16, 16, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(2000), new ThreadPoolExecutor.CallerRunsPolicy());
    
    //BASEMESH CLONE IS SENT WITH TRIANGLE COLOR FOR EACH TRIANGLE
    int[] project(float tri[], int[] uv){

        Arrays.fill(zbuffer,Float.MAX_VALUE); // zbuffer is set to max value after each frame
        Arrays.fill(triangle_index, -1);// triangle index is set to -1 soo if a pixel doesnt have any trianlge corresponding to it that pixel can be skipped by checking its triangle index (-1 triangle index is not possible )


        rejection = 0;
        float px;
        float py;
        float pz;
        float px1;
        float py1;
        float pz1;
        float sin1 = (float)Math.sin(-azimuthal_angle/2.0f);
        float sin2 = (float)Math.sin((1.570796f - polar_angle)/2.0f);
        float cos1 = (float)Math.cos(-azimuthal_angle/2.0f);
        float cos2 = (float)Math.cos((1.570796f - polar_angle)/2.0f);
        
        // NORMALIZATION LOOP 3D SCREEN IS ROTATED SOO THAT CAMERA IS AT 0 0 0 AND POINT IN POSITIVE X DIRECTION FOR PERSPECTIVE CALCULATION THIS USES SIMPLIFIED QUATERNEON 

        for (int i = 0; i < tri_count * 3 ; i++) {
            px = tri[3*i]     - cam_x; 
            py = tri[3*i + 1] - cam_y; 
            pz = tri[3*i + 2] - cam_z;

            px1 = px*(1-2*(sin1*sin1)) - 2*py*cos1*sin1;
            py1 = py*(1-2*(sin1*sin1)) + 2*px*cos1*sin1; 
            pz1 = pz;

            triangle[3*i]     = px1*(1-2*(sin2*sin2)) + 2*pz1*cos2*sin2;
            triangle[3*i + 1] = py1;
            triangle[3*i + 2] = pz1*(1-2*(sin2*sin2)) - 2*px1*cos2*sin2;
        }

        // BACKFACE CULLING LOOP EACH TRIANGLE IS TAKEN AND IF ITS BEHIND THE CAMERA IT GETS REJECTED AND EACH TRIANGLE IS PRECULLED, CULLING IS TESTED BY CHECKING THE VECTOR AREA SIGN 
        for(int i = 0 ; i < tri_count ; i++){
            x1 = triangle[9*i]    ; y1 = triangle[9*i + 1]; z1 = triangle[9*i + 2]; 
            x2 = triangle[9*i + 3]; y2 = triangle[9*i + 4]; z2 = triangle[9*i + 5]; 
            x3 = triangle[9*i + 6]; y3 = triangle[9*i + 7]; z3 = triangle[9*i + 8]; 
            if(x1 < 1.05f || x2 < 1.05f || x3 < 1.05f) { // DISTANCE REJECTION
                tri_isvisible[i] = false;
                //rejection++;
                continue;
                
            }
            x = (y2 - y1)*(z3 - z1) - (z2 - z1)*(y3 - y1);
            y = (x3 - x1)*(z2 - z1) - (x2 - x1)*(z3 - z1);
            z = (x2 - x1)*(y3 - y1) - (x3 - x1)*(y2 - y1); 
            w1 = (x1 + x2 + x3)/3;
            w2 = (y1 + y2 + y3)/3;
            w3 = (z1 + z2 + z3)/3;
            //CULLING REJECTION
            if(-(x*w1 + y*w2 + z*w3) > 1e-9f){ //DOT PRODUCT OF CAMERA POINT TO TRIANGLE'S CENTRE AND TRIANGLE NORMAL VECTOR CALCULATED BY CORRECT WINDING ORDER
                float mag = (float)Math.sqrt(x*x + y*y + z*z);
                float xx1 = data[24*i +  0] = y1*view_fac/(x1); //1 IF THE TEST IS PASSED AND TRIANGLE IS FACING THE CAMERA THEN THE PERSPECTIVE PROJECTION IS CALCULATED ON THE NDC PLANE AND ALONG WITH OTHER VARIABLES FOR ZBUFFER CALCUATION AND TEXTURE PROJECTION
                float xy1 = data[24*i +  1] = z1*view_fac/(x1); //2
                float xx2 = data[24*i +  2] = y2*view_fac/(x2); //3
                float xy2 = data[24*i +  3] = z2*view_fac/(x2); //4
                float xx3 = data[24*i +  4] = y3*view_fac/(x3); //5
                float xy3 = data[24*i +  5] = z3*view_fac/(x3); //6
                float x_min = ((Math.min(xx1, Math.min(xx2,xx3)))); 
                float x_max = ((Math.max(xx1, Math.max(xx2,xx3)))); 
                float y_min = ((Math.min(xy1, Math.min(xy2,xy3)))); 
                float y_max = ((Math.max(xy1, Math.max(xy2,xy3)))); 
                if (x_max < -1 || x_min > 1 || y_max < -1 || y_min > 1) { // IF TRIANGLES OUSIDE THE NDC PLANE ITS REJECTED 
                    //rejection++;
                    tri_isvisible[i] = false;
                    continue;
                }
                // AFTER PROJECTION AXIS ARE PERMULATED 3D SPACE Y - SCREEN X || 3D SPACE Z - SCREEN Y || 3D SPACE X - DEPTH

                data[24*i +  6] = y1; //7  3d space points after normalization Y1
                data[24*i +  7] = y2; //8  3d space points after normalization Y2
                data[24*i +  8] = y3; //9  3d space points after normalization Y3
                data[24*i +  9] = z1; //10 3d space points after normalization Z1
                data[24*i + 10] = z2; //11 3d space points after normalization Z2
                data[24*i + 11] = z3; //12 3d space points after normalization Z3
                data[24*i + 12] = x1; //13 3d space points after normalization X1
                data[24*i + 13] = x2; //14 3d space points after normalization X2
                data[24*i + 14] = x3; //15 3d space points after normalization X3

                data[24*i + 15] = x/mag; // 16 - 22 // NORMAL OF THE TRIANGLE IN NORMALIZED 3D SPACE
                data[24*i + 16] = y/mag; // 17 - 23 // NORMAL OF THE TRIANGLE IN NORMALIZED 3D SPACE
                data[24*i + 17] = z/mag; // 18 - 24 // NORMAL OF THE TRIANGLE IN NORMALIZED 3D SPACE

                data[i*24 + 18] = uv[6*i + 0]; // UV COORDINATES CORRESPONDING TO EACH VERTEX
                data[i*24 + 19] = uv[6*i + 1]; // UV COORDINATES CORRESPONDING TO EACH VERTEX
                data[i*24 + 20] = uv[6*i + 2]; // UV COORDINATES CORRESPONDING TO EACH VERTEX
                data[i*24 + 21] = uv[6*i + 3]; // UV COORDINATES CORRESPONDING TO EACH VERTEX
                data[i*24 + 22] = uv[6*i + 4]; // UV COORDINATES CORRESPONDING TO EACH VERTEX
                data[i*24 + 23] = uv[6*i + 5]; // UV COORDINATES CORRESPONDING TO EACH VERTEX
                tri_isvisible[i] = true;
            }else{
                //rejection++;
                tri_isvisible[i] = false;
            }
        }        
        for(int i = 0 ; i <16 ; i++){ //RESETING THE NUMBER OF TRIANGLE PERSENT IN EACH TILE BEFORE MULTITHREADED RASTERIZATION
            last_tri_index[i] = 0;
        }
        for(int i = 0 ; i < tri_count ; i++){ // ASSIGNING EACH TRIANGLE TO A TILE / TILES
            if(!tri_isvisible[i]) continue;
            float x_min = (float)((Math.min(data[i*24+0], Math.min(data[i*24+2],data[i*24+4])))); // CREATING BOUNDS
            float x_max = (float)((Math.max(data[i*24+0], Math.max(data[i*24+2],data[i*24+4])))); // CREATING BOUNDS
            float y_min = (float)((Math.min(data[i*24+1], Math.min(data[i*24+3],data[i*24+5])))); // CREATING BOUNDS
            float y_max = (float)((Math.max(data[i*24+1], Math.max(data[i*24+3],data[i*24+5])))); // CREATING BOUNDS
            for(int cy = 0 ; cy < 4 ; cy++){ //GOING OVER TILES IN Y AXIS
                float LYmin = (float)(cy-2.0f)/2.0f; // TILE BOUNDS IN Y AXIS MINIMUM
                float LYmax = (float)(cy-1.0f)/2.0f; // TILE BOUNDS IN Y AXIS MAXIMUM
                for(int cx = 0 ; cx < 4 ; cx++){//GOING OVER TILES IN X AXIS
                    float LXmin = (float)(cx-2.0f)/2.0f; // TILE BOUNDS IN X AXIS MINIMUM
                    float LXmax = (float)(cx-1.0f)/2.0f; // TILE BOUNDS IN X AXIS MAXIMUM
                    if(x_min < LXmax && x_max > LXmin && y_max > LYmin && y_min < LYmax) //COMPARING TILE BOUNDS AND TRIANGLE BOUND PER TILE
                        tri_chunk_index[cx + cy*4][last_tri_index[cx + cy*4]++] = i;
                       // last_tri_index[cx + cy*4] ++;
                }
            }
        }
        //HARDCODED TILE RASTERIZATION FOR 1000X1000 PIXEL TILES -> 4 X 4
        List<Callable<Void>> tiles = new ArrayList<>();
        //CREATING CALLABLEs
        tiles.add(() -> {mtr.chunk_rasterization(0  ,  0 ,  250,  250,  0, last_tri_index[0]) ; return null; });
        tiles.add(() -> {mtr.chunk_rasterization(250,  0 ,  500,  250,  1, last_tri_index[1]) ; return null; });
        tiles.add(() -> {mtr.chunk_rasterization(500,  0 ,  750,  250,  2, last_tri_index[2]) ; return null; });
        tiles.add(() -> {mtr.chunk_rasterization(750,  0 , 1000,  250,  3, last_tri_index[3]) ; return null; });

        tiles.add(() -> {mtr.chunk_rasterization(0  , 250,  250,  500,  4, last_tri_index[4]) ; return null; });
        tiles.add(() -> {mtr.chunk_rasterization(250, 250,  500,  500,  5, last_tri_index[5]) ; return null; });
        tiles.add(() -> {mtr.chunk_rasterization(500, 250,  750,  500,  6, last_tri_index[6]) ; return null; });
        tiles.add(() -> {mtr.chunk_rasterization(750, 250, 1000,  500,  7, last_tri_index[7]) ; return null; });

        tiles.add(() -> {mtr.chunk_rasterization(0  , 500,  250,  750,  8, last_tri_index[8] ); return null; });
        tiles.add(() -> {mtr.chunk_rasterization(250, 500,  500,  750,  9, last_tri_index[9] ); return null; });
        tiles.add(() -> {mtr.chunk_rasterization(500, 500,  750,  750, 10, last_tri_index[10]); return null; });
        tiles.add(() -> {mtr.chunk_rasterization(750, 500, 1000,  750, 11, last_tri_index[11]); return null; });

        tiles.add(() -> {mtr.chunk_rasterization(0  , 750,  250, 1000, 12, last_tri_index[12]); return null; });
        tiles.add(() -> {mtr.chunk_rasterization(250, 750,  500, 1000, 13, last_tri_index[13]); return null; });
        tiles.add(() -> {mtr.chunk_rasterization(500, 750,  750, 1000, 14, last_tri_index[14]); return null; });
        tiles.add(() -> {mtr.chunk_rasterization(750, 750, 1000, 1000, 15, last_tri_index[15]); return null; });

        try {
            List<Future<Void>> results = run2.invokeAll(tiles);  
            for (Future<Void> f : results) f.get();              //WAITING FOR EACH THREAD TO COMPLETE 
        } catch (InterruptedException | ExecutionException e) {
        }
        List<Callable<Void>> shade = new ArrayList<>();

        for(int i = 0; i < scanline; i++){
            final int a = i;
            shade.add(() -> {mtr.run1(a); return null;});// SCANLINE SHADING OVER 4 THREADS N, N+1 , N+2 , N+3 pixel scan line for even load distribution
        }
        try {
            List<Future<Void>> result1 = run1.invokeAll(shade);
            for(Future<Void> f : result1) f.get();                //WAITING FOR EACH THREAD TO COMPLETE 
        }catch (InterruptedException | ExecutionException e){
        }

        if(ASCIIeffect){
            return ASCII_(image);
        }
        return image;
    }
    int[] ASCII_(int[] image){
        int color;
        Arrays.fill(processed_image, 0);
        for(int ry = 0 ; ry < runy ; ry++){
            for(int rx = 0; rx < runx ; rx ++){
                int r = 0;
                int g = 0;
                int b = 0;
                for(int py = ry * 7 ; py < (ry + 1) * 7 ; py++){
                    for(int px = rx * 5 ; px < (rx + 1) * 5 ; px++){
                        int argb = image[py * pixel_x + px];
                        r += (argb >>> 16) & 0xFF;
                        g += (argb >>> 8)  & 0xFF;
                        b +=  argb         & 0xFF;
                    }
                }
                r /= 35;
                g /= 35;
                b /= 35;                
                int lum = (r * 77 + g * 150 + b * 29) >> 12;

                int max = Math.max(r, Math.max(g, b));
                if(max>0){
                    r = r*brightness/max;
                    g = g*brightness/max;
                    b = b*brightness/max;
                }
                color = 0xFF000000 | (r << 16) | (g << 8) | b;
                
                if(color == 0xFF000000) continue;
                for(int py = 0 ; py < 7 ; py++){
                    for(int px = 0 ; px < 5 ; px++){
                        if(FONT_BOOL[lum][py][px])
                        //System.out.println(color);
                        
                        processed_image[(py+ry*7) * pixel_x + rx*5 + px] = color;
                        
                    }
                }
            }
        }
        return processed_image;
    }
    
    boolean bound(float x1, float y1, float x2, float y2, float x3, float y3){
        float x_min = ((Math.min(x1, Math.min(x2,x3))));
        float x_max = ((Math.max(x1, Math.max(x2,x3))));
        float y_min = ((Math.min(y1, Math.min(y2,y3))));
        float y_max = ((Math.max(y1, Math.max(y2,y3))));
        if (x_max < -1 || x_min > 1 || y_max < -1 || y_min > 1) {
            return true;
        }
        return false;
    }
    void particle_render(){

    }
    
}
class multithreadrender{
    final int pixel_x;
    final int pixel_y;
    final render r;
    float scaling;
    int[][] texture;

    public multithreadrender(render r, int pixel_x, int pixel_y, int[][] texture){
        this.r = r;
        this.pixel_x = pixel_x;
        this.pixel_y = pixel_y;
        this.texture = texture;
        scaling = pixel_y/2.0f;
    }
    
    public void run1(int a){
        final float rh = 0.05f; final float ambient = 0.1f;
        //int p = (a%2 == 0 ) ? 1 : 0;
        final float shininess = 2.0f/(rh*rh + 0.0001f) - 2.0f ;
        for(int pxy = a ; pxy < pixel_y ; pxy+=4){
            for(int pxx = 0 ; pxx < pixel_x ; pxx++){
                if(r.triangle_index[pxy*pixel_x + pxx] < 0){
                    r.image[pxy * pixel_x + pxx] = 0xFF000000; 
                    continue ;
                }
                int index = r.triangle_index[pxy*pixel_x + pxx];
                float X1 = r.data[index*24+6];  float X2 = r.data[index*24+7];  float X3 = r.data[index*24+8];  //THIS IS THE NORMALIZED 3D SPACE POINTS
                float Y1 = r.data[index*24+9];  float Y2 = r.data[index*24+10]; float Y3 = r.data[index*24+11]; //THIS IS THE NORMALIZED 3D SPACE POINTS
                float Z1 = r.data[index*24+12]; float Z2 = r.data[index*24+13]; float Z3 = r.data[index*24+14]; //THIS IS THE NORMALIZED 3D SPACE POINTS
                float comz = r.zbuffer[pxy * pixel_x + pxx];
                float PX = ((pxx - pixel_x/2)*2.0f/pixel_y)*comz/r.view_fac; // point receration IN 3D SPACE BY EXTENDING THE 2X2 NDC PLANE POINT BY MULTIPLYING IT BY DEPTH/COMZ 
                float PY = ((pxy - pixel_y/2)*2.0f/pixel_y)*comz/r.view_fac;
                float PZ =   comz;

                float d00 = (X2 - X1)*(X2 - X1) + (Y2 - Y1)*(Y2 - Y1) + (Z2 - Z1)*(Z2 - Z1); //THIS IS THE CALCUTION OF BAYCENTRIC CORDS FROM TRIANGLE VERTRICS AND RECREATED POINT TO GET THE PROPER PESPECTIVE CORRECT TEXTURE MAPPING
                float d01 = (X2 - X1)*(X3 - X1) + (Y2 - Y1)*(Y3 - Y1) + (Z2 - Z1)*(Z3 - Z1); 
                float d11 = (X3 - X1)*(X3 - X1) + (Y3 - Y1)*(Y3 - Y1) + (Z3 - Z1)*(Z3 - Z1); 
                float d20 = (PX - X1)*(X2 - X1) + (PY - Y1)*(Y2 - Y1) + (PZ - Z1)*(Z2 - Z1); 
                float d21 = (PX - X1)*(X3 - X1) + (PY - Y1)*(Y3 - Y1) + (PZ - Z1)*(Z3 - Z1); 
                float denom = d00*d11 - d01*d01;
                float W2 = (d11*d20 - d01*d21) / denom;
                float W3 = (d00*d21 - d01*d20) / denom;
                float W1 = 1.0f - W2 - W3;
                float u1 = (W1*r.data[index*24+18] + W2*r.data[index*24+20] + W3*r.data[index*24+22]); // HAVE THE MAPPED PIXEL LOCATION OF THE TRIANGLE ON A UV TEXTURE AND USING THE CORRECT BAYCENTRIC CORDS ON THE UV COORDS WILL GIVE THE CORRECT PIXEL ON THE UV TEXTURE
                float v1 = (W1*r.data[index*24+19] + W2*r.data[index*24+21] + W3*r.data[index*24+23]); // HAVE THE MAPPED PIXEL LOCATION OF THE TRIANGLE ON A UV TEXTURE AND USING THE CORRECT BAYCENTRIC CORDS ON THE UV COORDS WILL GIVE THE CORRECT PIXEL ON THE UV TEXTURE
                int u = (int)u1; if (u < 0) u = 0; if (u > 64 - 1)  u = 64-1; //clamping of uv texture
                int v = (int)v1; if (v < 0) v = 0; if (v > 48 - 1)  v = 48-1; //clamping of uv texture

                float dot =  (PZ - r.LX)*r.data[index*24+15] + (PX - r.LY)*r.data[index*24+16] + (PY - r.LZ)*r.data[index*24+17]; //DOT PRODUCT BETWEEN MESH NORMAL VECTOR AND LIGHT RAY
                float refx = (PZ - r.LX) - 2.0f*dot*r.data[index*24+15]; // VECTOR OF REFLECTED LIGHT BEAM FROM MESH SURFACE
                float refy = (PX - r.LY) - 2.0f*dot*r.data[index*24+16]; // VECTOR OF REFLECTED LIGHT BEAM FROM MESH SURFACE
                float refz = (PY - r.LZ) - 2.0f*dot*r.data[index*24+17]; // VECTOR OF REFLECTED LIGHT BEAM FROM MESH SURFACE

                float magr =  (float)(Math.sqrt(refx*refx + refy*refy + refz*refz)); 
                float mag = (float)Math.sqrt(PX*PX + PY*PY + PZ*PZ);
                float reflection = -(refx * PZ + refy * PX + refz * PY)/(magr*mag); // DOT PRODUCT OF THE NORMALIZED REFLECTED LIGHT BEAM AND NORMALIZED CAMERA - MESH SURFACE VECTOR TO SEE THE MAGNITUDE OF LIGHT GETTING TO THE CAMERA
                float atten = 1.0f / ( 1.0f + 0.01f*magr + 0.0001f*magr*magr); 
                float diffuse = Math.max((-dot/magr) , 0.0f); 
                float spec = (float)Math.pow(Math.max(reflection,0), shininess); 
                spec *= (1-rh)*0.8f + 0.2f;
                int base = (int) texture[v][u];
                int r1 = (int)Math.min(255, (((base >> 16) & 0xFF)*(ambient + diffuse*atten) + 255f * spec *atten)); //SHADING EFFECT ON EACH RGB CHANNEL
                int g1 = (int)Math.min(255, (((base >>  8) & 0xFF)*(ambient + diffuse*atten) + 255f * spec *atten)); //SHADING EFFECT ON EACH RGB CHANNEL
                int b1 = (int)Math.min(255, (( base        & 0xFF)*(ambient + diffuse*atten) + 255f * spec *atten)); //SHADING EFFECT ON EACH RGB CHANNEL
                r.image[pxy * pixel_x + pxx] = (r1 << 16) | (g1 << 8) | b1; // PUTTING THE RGB ON THE FINAL IMAGE
            }
        }
    }
    void chunk_rasterization(int pixel_x_st, int pixel_y_st, int pixel_x_ed, int pixel_y_ed, int chunk_index, int last_tri_index){
        for(int i = 0 ; i < last_tri_index ; i++){
            int index = r.tri_chunk_index[chunk_index][i];
            if(r.data[24*index +  23] == -1) continue;
            float x1 = r.data[index*24+0] * scaling + pixel_x/2.0f; float y1 = r.data[index*24+1] * scaling + pixel_y/2.0f; float z1 = r.data[index*24+12];
            float x2 = r.data[index*24+2] * scaling + pixel_x/2.0f; float y2 = r.data[index*24+3] * scaling + pixel_y/2.0f; float z2 = r.data[index*24+13];
            float x3 = r.data[index*24+4] * scaling + pixel_x/2.0f; float y3 = r.data[index*24+5] * scaling + pixel_y/2.0f; float z3 = r.data[index*24+14];
            int x_min = (int)(Math.floor((Math.min(x1, Math.min(x2,x3))) - 1));
            int x_max = (int)(Math.ceil ((Math.max(x1, Math.max(x2,x3))) + 1));
            int y_min = (int)(Math.floor((Math.min(y1, Math.min(y2,y3))) - 1));
            int y_max = (int)(Math.ceil ((Math.max(y1, Math.max(y2,y3))) + 1));
            if(x_min < pixel_x_st) x_min = pixel_x_st;
            if(x_max > pixel_x_ed) x_max =  pixel_x_ed;
            if(y_min < pixel_y_st) y_min = pixel_y_st;
            if(y_max > pixel_y_ed) y_max =  pixel_y_ed;
            float denom = (x2 - x1)*(y3 - y2) - (x3 - x2)*(y2 - y1);
            if(Math.abs(denom) < 1e-9) continue;
            for(int pxy = y_min ; pxy < y_max ; pxy++){
                for(int pxx = x_min ; pxx < x_max ; pxx++){
                    float comz = r.zbuffer[pxy * pixel_x + pxx];
                    if(z1 > comz && z2 > comz && z3 > comz) continue;
                    float w3 = ((x2 - x1)*(pxy - y1) - (y2 - y1)*(pxx - x1))/denom;
                    float w1 = ((x3 - x2)*(pxy - y2) - (y3 - y2)*(pxx - x2))/denom;
                    float w2 = 1 - (w3 + w1);
                    if(w1 >= -1e-8 && w2 >= -1e-8 && w3 >= -1e-8){
                        float interpolatedReciprocalZ = w1*(1.0f/z1) + w2*(1.0f/z2) + w3*(1.0f/z3);
                        if(comz > 1.0f/interpolatedReciprocalZ){
                            r.zbuffer[pxy * pixel_x + pxx] = comz = 1/interpolatedReciprocalZ;
                            r.triangle_index[pxy*pixel_x + pxx] = index;
                        }
                    }
                }
            }
        }
    }
}

class viewer extends JPanel {
    float difficulty = 0.2f;
    
    render r;
    final int w, h, scale;
    final BufferedImage img;
    private final int[] dst;    // img's own pixel array
    private final int[] last;   // last frame, so X/Y can redraw it
    boolean flipX = false, flipY = true;   // false = fast path (one arraycopy)

    viewer(int w, int h, int scale, String title) {
        this.w = w; this.h = h; this.scale = scale;
        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        dst = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        last = new int[w * h];
        setPreferredSize(new Dimension(w * scale, h * scale));
        bind("X", () -> { flipX = !flipX; redraw(); });
        bind("Y", () -> { flipY = !flipY; redraw(); });
        bind("S", () -> save("frame.png"));
        bind("E", () -> {r.ASCIIeffect = !r.ASCIIeffect;});
        bind("P", () -> {r.brightness++; System.out.println(r.brightness);});
        bind("M", () -> {r.brightness--; System.out.println(r.brightness);});
        bind("Z", () -> {r.view_fac+=0.1f;});
        bind("A", () -> {r.view_fac-=0.1f;});


        JFrame f = new JFrame(title);
        f.add(this);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    private void bind(String key, Runnable r) { 
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        getActionMap().put(key, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { r.run(); }
        });
    }

    static void fill(int[] dst, int[] src, int w, int h, boolean flipX, boolean flipY) {
        if (!flipX && !flipY) { System.arraycopy(src, 0, dst, 0, w * h); return; }
        for (int y = 0; y < h; y++) {
            int sy = flipY ? h - 1 - y : y;
            for (int x = 0; x < w; x++) {
                int sx = flipX ? w - 1 - x : x;
                dst[sy * w + sx] = src[y * w + x];
            }
        }
    }

    private void redraw() {
        synchronized (img) { fill(dst, last, w, h, flipX, flipY); }
        repaint();
    }

    void show(int[] image) {
        System.arraycopy(image, 0, last, 0, last.length);
        redraw();
    }

    void save(String path) {
        try { synchronized (img) { ImageIO.write(img, "png", new File(path)); } System.out.println("saved " + path); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        synchronized (img) { g.drawImage(img, 0, 0, w * scale, h * scale, null); }
    }

    /** turntable helper: rotates src about the vertical (z) axis into dst, so project() never eats your base mesh */
    static void spinZ(float[] src, float[] dst, float ang) {
        float c = (float) Math.cos(ang), s = (float) Math.sin(ang);
        for (int i = 0; i < src.length / 3; i++) {
            float x = src[3*i], y = src[3*i+1];
            dst[3*i]   = x * c - y * s;
            dst[3*i+1] = x * s + y * c;
            dst[3*i+2] = src[3*i+2];
        }
    }
}
class MeshLoader { // loads mesh from the same folder
    public static float[] loadMesh(String filePath) {
        ArrayList<Float> tempValues = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                float val = Float.parseFloat(line);
                tempValues.add(val);
            }
        } catch (IOException e) {
            System.err.println("Error reading mesh file: " + e.getMessage());
        }
        float[] base_mesh = new float[tempValues.size()];
        for (int i = 0; i < tempValues.size(); i++) {
            base_mesh[i] = tempValues.get(i);
        }
        return base_mesh;
    }
    float[] load_default_mesh(float a){
        float h = a/2.0f;
        return  new float[]{-h,-h, h,  h,-h, h,  h, h, h,  -h,-h, h,  h, h, h,  -h, h, h,   h,-h,-h, -h,-h,-h, -h, h,-h,   h,-h,-h, -h, h,-h,  h, h,-h,   h,-h, h,  h,-h,-h,  h, h,-h,   h,-h, h,  h, h,-h,  h, h, h,  -h,-h,-h, -h,-h, h, -h, h, h,  -h,-h,-h, -h, h, h, -h, h,-h,  -h, h, h,  h, h, h,  h, h,-h,  -h, h, h,  h, h,-h, -h, h,-h,  -h,-h,-h,  h,-h,-h,  h,-h, h,  -h,-h,-h,  h,-h, h, -h,-h, h,};
    }
    int[] load_default_uv(){
        return new int[]  {63, 31,    48, 31,   48, 16,   63, 31,    48, 16,   63, 16,     31, 31,  16, 31,   16, 16,     31, 31,  16, 16,    31, 16,    47, 31,   32, 31,   32, 16,    47, 31,   32, 16,   47, 16,   15, 31,   0, 31,    0, 16,     15, 31,   0, 16,    15, 16,    16, 0,     31, 0,    31, 15,   16, 0,     31, 15,   16, 15,   16, 32,    31, 32,   31, 47,   16, 32,    31, 47,  16, 47   };

    }
    int[][] load_default_texture(){
        return new int[][] {
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF189B3B, 0xFF168533, 0xFF178A35, 0xFF168132, 0xFF189B3B, 0xFF168433, 0xFF189B3B, 0xFF168935, 0xFF168935, 0xFF189339, 0xFF189B3B, 0xFF189339, 0xFF19A23D, 0xFF14782F, 0xFF189339, 0xFF168935, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF189B3B, 0xFF168132, 0xFF189B3B, 0xFF168533, 0xFF19A23D, 0xFF593D29, 0xFF189B3B, 0xFF14782F, 0xFF19A13D, 0xFF178A35, 0xFF189B3B, 0xFF168734, 0xFF189A3B, 0xFF168233, 0xFF189B3B, 0xFF158132, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF178B35, 0xFF593D29, 0xFF19A23D, 0xFF157A30, 0xFF189B3B, 0xFF593D29, 0xFF168533, 0xFF593D29, 0xFF189B3B, 0xFF189339, 0xFF178A35, 0xFF19A03D, 0xFF593D29, 0xFF146F2C, 0xFF178A35, 0xFF593D29, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF593D29, 0xFF6C6C6C, 0xFF593D29, 0xFF593D29, 0xFF168734, 0xFF593D29, 0xFF593D29, 0xFF593D29, 0xFF14762F, 0xFF593D29, 0xFF168233, 0xFF593D29, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF593D29, 0xFF6C6C6C, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF878787, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF593D29, 0xFFB9855C, 0xFFB9855C, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF878787, 0xFF79553A, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF6C6C6C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF3F48CC, 0xFF3F48CC, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF745844, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF593D29, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF3F48CC, 0xFF3F48CC, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF3F48CC, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF3F48CC, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF878787, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF168935, 0xFF158132, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF593D29, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFFB9855C, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF158132, 0xFF168935, 0xFF4E772A, 0xFF679D38, 0xFF4E762A, 0xFF476C26, 0xFF476C26, 0xFF4C7329, 0xFF436624, 0xFF4A7128, 0xFF547F2D, 0xFF4F792B, 0xFF496F28, 0xFF476D27, 0xFF496E27, 0xFF588630, 0xFF3B5A20, 0xFF4B7128 },
            { 0xFF189339, 0xFF189B3B, 0xFF178A35, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF878787, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF878787, 0xFF593D29, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF878787, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF178A35, 0xFF189B3B, 0xFF189339, 0xFF4F782B, 0xFF4A7128, 0xFF55812E, 0xFF50792B, 0xFF517B2C, 0xFF446725, 0xFF517B2C, 0xFF456925, 0xFF436624, 0xFF598830, 0xFF588530, 0xFF4E762A, 0xFF679D38, 0xFF4A7128, 0xFF476D27, 0xFF4A7028 },
            { 0xFF14782F, 0xFF168233, 0xFF146F2C, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF146F2C, 0xFF168233, 0xFF14782F, 0xFF56832F, 0xFF4B7128, 0xFF426424, 0xFF466A26, 0xFF496E27, 0xFF486D27, 0xFF4C7429, 0xFF4C7429, 0xFF4E772A, 0xFF54802E, 0xFF598730, 0xFF5C8B32, 0xFF476D27, 0xFF3F6022, 0xFF446725, 0xFF609234 },
            { 0xFF19A23D, 0xFF189A3B, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF6C6C6C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF189A3B, 0xFF19A23D, 0xFF3E5F22, 0xFF466B26, 0xFF496F28, 0xFF50792B, 0xFF4D752A, 0xFF517B2C, 0xFF426424, 0xFF4D752A, 0xFF436624, 0xFF456926, 0xFF4B7128, 0xFF50792B, 0xFF4C7329, 0xFF4A7028, 0xFF588630, 0xFF426424 },
            { 0xFF189339, 0xFF168734, 0xFF19A03D, 0xFF593D29, 0xFF6C6C6C, 0xFF593D29, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF6C6C6C, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF593D29, 0xFF6C6C6C, 0xFF593D29, 0xFF19A03D, 0xFF168734, 0xFF189339, 0xFF56822E, 0xFF456926, 0xFF5C8B32, 0xFF456926, 0xFF5B8A31, 0xFF5C8C32, 0xFF486D27, 0xFF5B8A31, 0xFF629635, 0xFF3E5E21, 0xFF446725, 0xFF588630, 0xFF5B8931, 0xFF436524, 0xFF486D27, 0xFF4B7128 },
            { 0xFF189B3B, 0xFF189B3B, 0xFF178A35, 0xFF168233, 0xFF593D29, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF593D29, 0xFF168233, 0xFF178A35, 0xFF189B3B, 0xFF189B3B, 0xFF517B2C, 0xFF507A2C, 0xFF406022, 0xFF588530, 0xFF446825, 0xFF4F792B, 0xFF4F782B, 0xFF527D2C, 0xFF476D27, 0xFF4F782B, 0xFF486D27, 0xFF456926, 0xFF406022, 0xFF486D27, 0xFF4B7229, 0xFF4E772A },
            { 0xFF189339, 0xFF178A35, 0xFF189339, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF878787, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF189339, 0xFF178A35, 0xFF189339, 0xFF436524, 0xFF537D2D, 0xFF57852F, 0xFF4C7429, 0xFF4D752A, 0xFF456926, 0xFF496F28, 0xFF436624, 0xFF466A26, 0xFF476D27, 0xFF609234, 0xFF5B8A31, 0xFF4D752A, 0xFF588630, 0xFF50792B, 0xFF4C7329 },
            { 0xFF168935, 0xFF19A13D, 0xFF189B3B, 0xFF14762F, 0xFF593D29, 0xFF593D29, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF6C6C6C, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF6C6C6C, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF593D29, 0xFF593D29, 0xFF14762F, 0xFF189B3B, 0xFF19A13D, 0xFF168935, 0xFF4C7329, 0xFF476D27, 0xFF537E2D, 0xFF4A7028, 0xFF4C7429, 0xFF426423, 0xFF57842F, 0xFF466A26, 0xFF50792B, 0xFF5C8B32, 0xFF476D27, 0xFF446825, 0xFF4C7329, 0xFF55812E, 0xFF436624, 0xFF598830 },
            { 0xFF168935, 0xFF14782F, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFFB9855C, 0xFF593D29, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF14782F, 0xFF168935, 0xFF588630, 0xFF527D2C, 0xFF609234, 0xFF5A8931, 0xFF5B8A31, 0xFF446825, 0xFF5C8B32, 0xFF537D2D, 0xFF5F9033, 0xFF5A8931, 0xFF456925, 0xFF4D752A, 0xFF466B26, 0xFF4E762A, 0xFF547F2D, 0xFF436524 },
            { 0xFF189B3B, 0xFF189B3B, 0xFF168533, 0xFF593D29, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFF878787, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF878787, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFFB9855C, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF593D29, 0xFF168533, 0xFF189B3B, 0xFF189B3B, 0xFF50792B, 0xFF456925, 0xFF619334, 0xFF4B7128, 0xFF4A7128, 0xFF507A2C, 0xFF5A8931, 0xFF446725, 0xFF476D27, 0xFF4D752A, 0xFF4D752A, 0xFF598830, 0xFF4B7229, 0xFF5F9033, 0xFF4C7329, 0xFF446825 },
            { 0xFF168433, 0xFF593D29, 0xFF593D29, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF593D29, 0xFFB9855C, 0xFFB9855C, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF878787, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF593D29, 0xFF593D29, 0xFF593D29, 0xFF168433, 0xFF527D2C, 0xFF547F2D, 0xFF50792B, 0xFF57852F, 0xFF456925, 0xFF609234, 0xFF4B7229, 0xFF4E762A, 0xFF669A37, 0xFF406123, 0xFF588630, 0xFF456926, 0xFF4C7329, 0xFF486D27, 0xFF50792B, 0xFF4C7329 },
            { 0xFF189B3B, 0xFF19A23D, 0xFF189B3B, 0xFF168734, 0xFF593D29, 0xFF79553A, 0xFF878787, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF745844, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF6C6C6C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF745844, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF878787, 0xFF79553A, 0xFF593D29, 0xFF168734, 0xFF189B3B, 0xFF19A23D, 0xFF189B3B, 0xFF466A26, 0xFF5C8B32, 0xFF476C26, 0xFF56832F, 0xFF50792B, 0xFF466A26, 0xFF4D752A, 0xFF496E27, 0xFF50792B, 0xFF4D752A, 0xFF4A7128, 0xFF416323, 0xFF5F9033, 0xFF436624, 0xFF426424, 0xFF496F28 },
            { 0xFF168132, 0xFF168533, 0xFF157A30, 0xFF593D29, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF593D29, 0xFF157A30, 0xFF168533, 0xFF168132, 0xFF629635, 0xFF50792B, 0xFF446825, 0xFF496F28, 0xFF517B2C, 0xFF4D752A, 0xFF466B26, 0xFF416223, 0xFF496F28, 0xFF4D752A, 0xFF496F28, 0xFF426424, 0xFF54802E, 0xFF57852F, 0xFF406123, 0xFF50792B },
            { 0xFF178A35, 0xFF189B3B, 0xFF19A23D, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF745844, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF593D29, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF593D29, 0xFF19A23D, 0xFF189B3B, 0xFF178A35, 0xFF446825, 0xFF598730, 0xFF3E5F22, 0xFF4D752A, 0xFF426424, 0xFF517B2C, 0xFF446825, 0xFF679D38, 0xFF55812E, 0xFF4E762A, 0xFF598730, 0xFF527D2C, 0xFF55812E, 0xFF486D27, 0xFF598730, 0xFF5C8C32 },
            { 0xFF168533, 0xFF168132, 0xFF593D29, 0xFF6C6C6C, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF6C6C6C, 0xFF593D29, 0xFF168132, 0xFF168533, 0xFF56832F, 0xFF55812E, 0xFF669A37, 0xFF416323, 0xFF5F9033, 0xFF5B8931, 0xFF4E772A, 0xFF4E762A, 0xFF436524, 0xFF4F782B, 0xFF4F782B, 0xFF476D27, 0xFF598830, 0xFF537E2D, 0xFF4A7028, 0xFF406123 },
            { 0xFF189B3B, 0xFF189B3B, 0xFF178B35, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF878787, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF178B35, 0xFF189B3B, 0xFF189B3B, 0xFF466B26, 0xFF4F792B, 0xFF486D27, 0xFF496E27, 0xFF57842F, 0xFF507A2C, 0xFF4E772A, 0xFF4E762A, 0xFF4B7229, 0xFF659A37, 0xFF426423, 0xFF598830, 0xFF466A26, 0xFF4C7429, 0xFF486D27, 0xFF547F2D },
            { 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF878787, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E },
            { 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFF22B14C, 0xFFED1C24, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFED1C24, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFA349A4, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E, 0xFFFFC90E },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF745844, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFB9855C, 0xFF593D29, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF6C6C6C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFFB9855C, 0xFF966C4A, 0xFF593D29, 0xFFB9855C, 0xFFB9855C, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF878787, 0xFF79553A, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF966C4A, 0xFF966C4A, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFFB9855C, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFF878787, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFFB9855C, 0xFFB9855C, 0xFF79553A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF79553A, 0xFF593D29, 0xFF966C4A, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF79553A, 0xFF79553A, 0xFF79553A, 0xFFB9855C, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF966C4A, 0xFF79553A, 0xFF966C4A, 0xFFB9855C, 0xFF593D29, 0xFF966C4A, 0xFF79553A, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF593D29, 0xFF6C6C6C, 0xFF79553A, 0xFF966C4A, 0xFF593D29, 0xFF79553A, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF593D29, 0xFF6C6C6C, 0xFF593D29, 0xFF593D29, 0xFF168734, 0xFF593D29, 0xFF593D29, 0xFF593D29, 0xFF14762F, 0xFF593D29, 0xFF168233, 0xFF593D29, 0xFF79553A, 0xFF593D29, 0xFF593D29, 0xFF79553A, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF178B35, 0xFF593D29, 0xFF19A23D, 0xFF157A30, 0xFF189B3B, 0xFF593D29, 0xFF168533, 0xFF593D29, 0xFF189B3B, 0xFF189339, 0xFF178A35, 0xFF19A03D, 0xFF593D29, 0xFF146F2C, 0xFF178A35, 0xFF593D29, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF189B3B, 0xFF168132, 0xFF189B3B, 0xFF168533, 0xFF19A23D, 0xFF593D29, 0xFF189B3B, 0xFF14782F, 0xFF19A13D, 0xFF178A35, 0xFF189B3B, 0xFF168734, 0xFF189A3B, 0xFF168233, 0xFF189B3B, 0xFF158132, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 },
            { 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFFED1C24, 0xFFED1C24, 0xFF189B3B, 0xFF168533, 0xFF178A35, 0xFF168132, 0xFF189B3B, 0xFF168433, 0xFF189B3B, 0xFF168935, 0xFF168935, 0xFF189339, 0xFF189B3B, 0xFF189339, 0xFF19A23D, 0xFF14782F, 0xFF189339, 0xFF168935, 0xFFED1C24, 0xFFED1C24, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000 }
        };
    }
}
