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

public class run{
    public static void main(String[] args) throws Exception{
        double K1 = -1.1623; //hell 
        double K2 = -5.0075; //hell 
        double K3 = 55.3019; //hell 
        double K4 = 9.1142;  //hell 
        double dt = 0.001;

        environment env = new environment("");env.reset((float)0.5);
        control ctrl = new control(env.M, env.m, env.L, K1, K2, K3 ,K4);
        env.reset(0.2f);
        
        
        float[] base_mesh = MeshLoader.loadMesh("mesh.bin");
        //float[] icosahedron = {0.0f , 0.0f , -0.6399999856948853f , 0.4631039798259735f , -0.33646076917648315f , -0.286217600107193f , -0.1768863946199417f , -0.5444096326828003f , -0.286217600107193f , 0.4631039798259735f , -0.33646076917648315f , -0.286217600107193f , 0.0f , 0.0f , -0.6399999856948853f , 0.4631039798259735f , 0.33646076917648315f , -0.286217600107193f , 0.0f , 0.0f , -0.6399999856948853f , -0.1768863946199417f , -0.5444096326828003f , -0.286217600107193f , -0.5724319815635681f , 0.0f , -0.286217600107193f , 0.0f , 0.0f , -0.6399999856948853f , -0.5724319815635681f , 0.0f , -0.286217600107193f , -0.1768863946199417f , 0.5444096326828003f , -0.286217600107193f , 0.0f , 0.0f , -0.6399999856948853f , -0.1768863946199417f , 0.5444096326828003f , -0.286217600107193f , 0.4631039798259735f , 0.33646076917648315f , -0.286217600107193f , 0.4631039798259735f , -0.33646076917648315f , -0.286217600107193f , 0.4631039798259735f , 0.33646076917648315f , -0.286217600107193f , 0.5724319815635681f , 0.0f , 0.286217600107193f , -0.1768863946199417f , -0.5444096326828003f , -0.286217600107193f , 0.4631039798259735f , -0.33646076917648315f , -0.286217600107193f , 0.1768863946199417f , -0.5444096326828003f , 0.286217600107193f , -0.5724319815635681f , 0.0f , -0.286217600107193f , -0.1768863946199417f , -0.5444096326828003f , -0.286217600107193f , -0.4631039798259735f , -0.33646076917648315f , 0.286217600107193f , -0.1768863946199417f , 0.5444096326828003f , -0.286217600107193f , -0.5724319815635681f , 0.0f , -0.286217600107193f , -0.4631039798259735f , 0.33646076917648315f , 0.286217600107193f , 0.4631039798259735f , 0.33646076917648315f , -0.286217600107193f , -0.1768863946199417f , 0.5444096326828003f , -0.286217600107193f , 0.1768863946199417f , 0.5444096326828003f , 0.286217600107193f , 0.4631039798259735f , -0.33646076917648315f , -0.286217600107193f , 0.5724319815635681f , 0.0f , 0.286217600107193f , 0.1768863946199417f , -0.5444096326828003f , 0.286217600107193f , -0.1768863946199417f , -0.5444096326828003f , -0.286217600107193f , 0.1768863946199417f , -0.5444096326828003f , 0.286217600107193f , -0.4631039798259735f , -0.33646076917648315f , 0.286217600107193f , -0.5724319815635681f , 0.0f , -0.286217600107193f , -0.4631039798259735f , -0.33646076917648315f , 0.286217600107193f , -0.4631039798259735f , 0.33646076917648315f , 0.286217600107193f , -0.1768863946199417f , 0.5444096326828003f , -0.286217600107193f , -0.4631039798259735f , 0.33646076917648315f , 0.286217600107193f , 0.1768863946199417f , 0.5444096326828003f , 0.286217600107193f , 0.4631039798259735f , 0.33646076917648315f , -0.286217600107193f , 0.1768863946199417f , 0.5444096326828003f , 0.286217600107193f , 0.5724319815635681f , 0.0f , 0.286217600107193f , 0.1768863946199417f , -0.5444096326828003f , 0.286217600107193f , 0.5724319815635681f , 0.0f , 0.286217600107193f , 0.0f , 0.0f , 0.6399999856948853f , -0.4631039798259735f , -0.33646076917648315f , 0.286217600107193f , 0.1768863946199417f , -0.5444096326828003f , 0.286217600107193f , 0.0f , 0.0f , 0.6399999856948853f , -0.4631039798259735f , 0.33646076917648315f , 0.286217600107193f , -0.4631039798259735f , -0.33646076917648315f , 0.286217600107193f , 0.0f , 0.0f , 0.6399999856948853f , 0.1768863946199417f , 0.5444096326828003f , 0.286217600107193f , -0.4631039798259735f , 0.33646076917648315f , 0.286217600107193f , 0.0f , 0.0f , 0.6399999856948853f , 0.5724319815635681f , 0.0f , 0.286217600107193f , 0.1768863946199417f , 0.5444096326828003f , 0.286217600107193f , 0.0f , 0.0f , 0.6399999856948853f};
        int[] color = new int[base_mesh.length/9];
        for(int i = 0 ; i < base_mesh.length/9 ; i++){
            color[i] = 0xFFFFFFFF;//(int)(Math.random()*35514566);
        }
        float fov = 90;
        float zoom = 3;
        int[] mesh_index = {0, 36*3, 72*3, 324};

        render r = new render(mesh_index[mesh_index.length - 1], 1000, 1000, fov, zoom, -12f, 0.0f, 7f, 2.25f, 0.0f);
        
        viewer v = new viewer(r.pixel_x, r.pixel_y, 1, "render");
        v.env = env;
        v.ctrl = ctrl;
        r.LX = -10f;
        r.LY = 10f;
        float[] basemesh_clone = base_mesh.clone();
        for (int f = 0;  ; f++) {
            double force = ctrl.computeControl(env.x , env.xd, env.theta, env.thetad);
            env.rk4(force, dt);
            if(f % 50 == 0){
                //env.printnn(f, force);
            
                basemesh_clone = base_mesh.clone();
                r.editmesh(base_mesh, basemesh_clone,mesh_index[0], mesh_index[1], (float)env.theta, -1.5707963268f, 1.5707963268f, 0, 0, 0);
                r.editmesh(basemesh_clone, basemesh_clone,mesh_index[0], mesh_index[2], 0, 0, 0, (float)env.x, 0, 0);
                r.editmesh(basemesh_clone, basemesh_clone,mesh_index[0], mesh_index[3], (float)Math.toRadians(80), 0, 0, 0, 0, 0);
                r.camedit(fov,4, -2.1f*5f, 0.0f, 1.225f*5f, 2.17f, 2.087f, 0.0f);
                r.project(basemesh_clone, color);
                v.show(r.image);
                try { Thread.sleep(20); } catch (InterruptedException e) {}
            }
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

    multithreadrender mtr; // multithreader object

    int scanline = 4; // out of 4 useful for crt effect

    int[][] tri_chunk_index; //it stored index of triangle for each tile

    public render(int pointcount , int resox, int resoy, float fov, float zoom, float cam_x, float cam_y ,float cam_z, float polar_angle,float azimuthal_angle){
        tri_count = pointcount/9;
        triangle = new float[pointcount];
        pixel_x = resox;
        pixel_y = resoy;
        triangle_index = new int[resox * resoy];
        image = new int[resox * resoy];
        zbuffer = new float[resox*resoy];
        data = new float[tri_count*24];
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
        mtr = new multithreadrender(this, pixel_x, pixel_y);
    }
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
    void project(float tri[], int[] color){

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
                data[24*i +  23] = -1;
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
                    data[24*i +  23] = -1;
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

                data[i*24 + 18] = color[i]; //uv[6*i + 0]; //16
                data[i*24 + 19] = 0; //uv[6*i + 1]; //17
                data[i*24 + 20] = 0; //uv[6*i + 2]; //18
                data[i*24 + 21] = 0; //uv[6*i + 3]; //19
                data[i*24 + 22] = 0; //uv[6*i + 4]; //20
                data[i*24 + 23] = 0; //uv[6*i + 5]; //21
            }else{
                //rejection++;
                data[24*i +  23] = -1;
            }
        }        
        for(int i = 0 ; i <16 ; i++){ //RESETING THE NUMBER OF TRIANGLE PERSENT IN EACH TILE BEFORE MULTITHREADED RASTERIZATION
            last_tri_index[i] = 0;
        }
        for(int i = 0 ; i < tri_count ; i++){ // ASSIGNING EACH TRIANGLE TO A TILE / TILES
            if(data[24*i +  23] == -1) continue;
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

    public multithreadrender(render r, int pixel_x, int pixel_y){
        this.r = r;
        this.pixel_x = pixel_x;
        this.pixel_y = pixel_y;
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
                float u1 = (W1*r.data[index*24+15] + W2*r.data[index*24+17] + W3*r.data[index*24+19]); // HAVE THE MAPPED PIXEL LOCATION OF THE TRIANGLE ON A UV TEXTURE AND USING THE CORRECT BAYCENTRIC CORDS ON THE UV COORDS WILL GIVE THE CORRECT PIXEL ON THE UV TEXTURE
                float v1 = (W1*r.data[index*24+16] + W2*r.data[index*24+18] + W3*r.data[index*24+20]); // HAVE THE MAPPED PIXEL LOCATION OF THE TRIANGLE ON A UV TEXTURE AND USING THE CORRECT BAYCENTRIC CORDS ON THE UV COORDS WILL GIVE THE CORRECT PIXEL ON THE UV TEXTURE
                int u = (int)u1; if (u < 0) u = 0; if (u > pixel_x - 1)  u = pixel_x-1; //clamping of uv texture
                int v = (int)v1; if (v < 0) v = 0; if (v > pixel_y - 1)  v = pixel_y-1; //clamping of uv texture

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
                int base = (int) r.data[index*24 + 18];
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
    environment env;
    control ctrl;
    environment1 env1;
    float difficulty = 0.2f;
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
        bind("R", () -> {env1.reset(difficulty);
            ctrl.initial = true; ctrl.initial_note_boolean = true;
            ctrl.drive = true; ctrl.hook = false; ctrl.relief = false;
            ctrl.current = 0;  ctrl.dhrcyclecount = 0;
                });
        bind("P", () -> {difficulty+=.2f; System.out.println(difficulty);});
        bind("M", () -> {difficulty-=.2f; System.out.println(difficulty);});
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
}
class environment{
    double M;
    double m;
    double L;
    double xdd;
    double xd;
    double x;
    double thetadd;
    double thetad;
    double theta;
    double g = 9.7905;
    double x_max;
    boolean noise_sw;
    double noise;
    String ANSI_RED = "\u001B[31m";
    String ANSI_RESET = "\u001B[0m";
    String ANSI_GREEN = "\033[32m";
    public environment(double mass_cart, double mass_rod, double length_rod, double initial_cart_acceleration, double initial_cart_velocity, double initial_cart_position, double initial_rod_angular_acceleration, double initial_rod_angular_velocity, double initial_rod_theta, double max_cart_displacement, double noise_fc){
        M = mass_cart; m = mass_rod; L = length_rod;
        xdd = initial_cart_acceleration; xd = initial_cart_velocity; x = initial_cart_position;
        thetadd = initial_rod_angular_acceleration; thetad = initial_rod_angular_velocity; theta = initial_rod_theta;
        x_max = max_cart_displacement; noise = noise_fc; noise_sw = true;
    }
    public environment(double mass_cart, double mass_rod, double length_rod, double initial_cart_acceleration, double initial_cart_velocity, double initial_cart_position, double initial_rod_angular_acceleration, double initial_rod_angular_velocity, double initial_rod_theta, double max_cart_displacement){
        M = mass_cart; m = mass_rod; L = length_rod;
        xdd = initial_cart_acceleration; xd = initial_cart_velocity; x = initial_cart_position;
        thetadd = initial_rod_angular_acceleration; thetad = initial_rod_angular_velocity; theta = initial_rod_theta;
        x_max = max_cart_displacement; noise_sw = false;
    }
    public environment(String defaultconfig){
        M = 1; m = 1; L = 1.5;
        xdd = 0; xd = 0.1; x = 0;
        thetadd = 0; thetad = 0.1; theta = Math.toRadians(0);
        x_max = 2; noise_sw = false;
    }

    void print(double dt, double force, boolean method){
        System.out.print("time(ms): " + dt + " | force: " + force + " | xdd: " + xdd + " | xd: " + xd + " | x: " + x + " | thetadd: " + thetadd + " | thetad: " + thetad + " | theta: " + theta);
        if(method){
            System.out.println(" Method: lqr");
        }else{
            System.out.println(" Method: Swingup");
        }
    }
    void printnn(double dt, double force){
        System.out.println("time(ms): " + dt + " | force: " + force + " | xdd: " + xdd + " | xd: " + xd + " | x: " + x + " | thetadd: " + thetadd + " | thetad: " + thetad + " | theta: " + theta);
    }
    private void calc(double x1, double xd1, double theta1, double thetad1, double force){
        double a1 = m + M;
        double b1 = m*L*Math.cos(theta1)/2;
        double c1 = -((m*L*thetad1*thetad1*Math.sin(theta1)/2) + force);
        double a2 = m*L*Math.cos(theta1)/2;
        double b2 = m*L*L/3;
        double c2 = m*L*g*Math.sin(theta1)/2;
        double denom = a1*b2 - a2*b1;
        xdd = (b1*c2 - b2*c1)/denom;
        thetadd = (c1*a2 - c2*a1)/denom;
    }

    void reset(float difficulty){
        x = (Math.random() * 2*difficulty - difficulty);
        xd = (Math.random() * difficulty - difficulty/2);
        theta = (Math.random() * 2*difficulty - difficulty);
        thetad = (Math.random() * 2*difficulty - difficulty);
        xdd = 0;
        thetadd = 0;
    }
    void resetpi(float difficulty){ // difficuily shuldnt be over 0.45 itll goo ut of lqr catch range :3
        x = (Math.random() * 2*2.5 - 2.5);
        xd = (Math.random() * difficulty - difficulty/2);
        theta = (Math.random() * 2*difficulty - difficulty) + 3.14;
        thetad = (Math.random() * 2*difficulty - difficulty);
        xdd = 0;
        thetadd = 0;
    }

    void rk4(double force, double dt){
        if(x > 3){
            force = 0;
            if(xd>0) xd = 0;
        }
        if(x < -3){
            force = 0;
            if(xd<0) xd = 0;
        }
        double       x1 = x;
        double      xd1 = xd;
        double   theta1 = theta;
        double  thetad1 = thetad;
        calc(x1, xd1, theta1, thetad1, force);
        double     xdd1 = xdd;
        double thetadd1 = thetadd;

        double       x2 = x +           xd1*(dt/2);
        double      xd2 = xd +         xdd1*(dt/2);
        double   theta2 = theta +   thetad1*(dt/2);
        double  thetad2 = thetad + thetadd1*(dt/2);
        calc(x2, xd2, theta2, thetad2, force);
        double     xdd2 = xdd;
        double thetadd2 = thetadd;

        double       x3 = x +           xd2*(dt/2);
        double      xd3 = xd +         xdd2*(dt/2);
        double   theta3 = theta +   thetad2*(dt/2);
        double  thetad3 = thetad + thetadd2*(dt/2);
        calc(x3, xd3, theta3, thetad3, force);
        double     xdd3 = xdd;
        double thetadd3 = thetadd;

        double       x4 = x +           xd3*(dt);
        double      xd4 = xd +         xdd3*(dt);
        double   theta4 = theta +   thetad3*(dt);
        double  thetad4 = thetad + thetadd3*(dt);
        calc(x4, xd4, theta4, thetad4, force);
        double     xdd4 = xdd;
        double thetadd4 = thetadd;

        x      += (dt/6.0)*(xd1 + 2*xd2 + 2*xd3 + xd4);
        xd     += (dt/6.0)*(xdd1 + 2*xdd2 + 2*xdd3 + xdd4);
        theta  += (dt/6.0)*(thetad1 + 2*thetad2 + 2*thetad3 + thetad4);
        thetad += (dt/6.0)*(thetadd1 + 2*thetadd2 + 2*thetadd3 + thetadd4);
    }
}
class control{
    double M;
    double m;
    double L;
    double g=9.7905;
    double K1,K2,K3,K4;
    double threshold = 0.4;
    double catchThreshold = Math.toRadians(45);
    double maxForce = 30;
    boolean method = false;

    double brkdistance = 0.45;
    double brkc = 20;
    double accc = 12.2;
    boolean initial = true;
    double target;
    double idkwhat = 0.15;
    double vdanger = 9.5;
    double swingdistance = 0.75;
    int wait = 100; //millisec, this gud shi
    int current = 0;
    double dhrcyclecount = 0;
    double xinitial;
    boolean initial_note_boolean = true;

    boolean drive = true;
    boolean hook = false;
    boolean relief = false;

    double Max_length = 2; //aka +-2 this is just to trick the controller it always misbehaves its actually +-3
    public control(double Mass, double mass, double Length, double k1, double k2, double k3, double k4)throws Exception{
        this.M = Mass;
        this.m = mass;
        this.L = Length;
        this.K1 = k1;
        this.K2 = k2;
        this.K3 = k3;
        this.K4 = k4;
    }
    public double computeControl(double x, double xd, double theta, double thetad) {
        method = true;
        double error = theta - Math.PI;
        error = Math.atan2(Math.sin(error), Math.cos(error));
        double lqrForce = -(K1 * x + K2 * xd + K3 * error + K4 * thetad);
        if (Math.abs(error) < threshold) {
            return Math.max(-maxForce, Math.min(maxForce, lqrForce));
        }
        if (Math.abs(error) < catchThreshold) {
            double catchForce = -(K3 * error + K4 * thetad);
            return Math.max(-maxForce, Math.min(maxForce, catchForce));
        }
        method = false;
        return swingUpController(x, xd, theta, thetad, error); 
    }

    private double swingUpController(double x, double xd, double theta, double thetad, double err) {
        if (initial) {
            
            if (theta * thetad > 0) {
                if (thetad > 0) {
                    target = -swingdistance * Max_length;
                } else {
                    target = swingdistance * Max_length;
                }
                initial_note_boolean = true; 
                initial = false;
                hook = false;
                drive = true;
                relief = false;
            } else {
                if (initial_note_boolean) {
                    xinitial = x;
                    initial_note_boolean = false;
                }
                target = xinitial;
                hold_lqr(target, x, xd);
            }
        }
        if (drive) {
            if (Math.abs(target - x) < idkwhat) {
                drive = false;
                hook = true;
                relief = false;
            }
            return driveforce(target, x, xd);
        }
        if (hook) {
            double sign = Math.signum(x);
            if (sign * thetad < 0) {
                hook = false;
                drive = false;
                relief = true;
                current = 0;
            }
            return hold_lqr(target, x, xd);
        }
        if (relief) {
            double sign = Math.signum(x);
            if(current == wait){
                hook = false;
                drive = true;
                relief = false;
                dhrcyclecount ++;
                target = -(sign) * Max_length * swingdistance;
            }
            current++;
            return hold_lqr(target, x, xd);
        }
        return 0.0; 
    }
    private double hold_lqr(double target, double x, double xd) {
        double KK1 = 80.06;
        double KK2 = 81.06; 
        return Math.max(-maxForce, Math.min(maxForce, -((x - target) * KK1 + xd * KK2)));
    }

    private double driveforce(double target, double x, double xd) {
        double xerr = target - x;

        if (Math.abs(xerr) < brkdistance){
            return Math.max(-maxForce, Math.min(maxForce, -xd * brkc));
        } 
        if(Math.abs(xerr) > brkdistance && Math.abs(xd) < vdanger ){
            return Math.max(-maxForce, Math.min(maxForce, accc * (target - x)));
        }
        return 0.0;
    }

}