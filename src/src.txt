import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
public class function{
    public static void main(String[] args){
        double[] uv = {64, 32,    48, 32,   48, 16,   64, 32,    48, 16,   64, 16,     32, 32,  16, 32,   16, 16,     32, 32,  16, 16,    32, 16,    48, 32,   32, 32,   32, 16,    48, 32,   32, 16,   48, 16,   16, 32,   0, 32,    0, 16,     16, 32,   0, 16,    16, 16,    16, 0,     32, 0,    32, 16,   16, 0,     32, 16,   16, 16,   16, 32,    32, 32,   32, 48,   16, 32,    32, 48,  16, 48   };
        Print P = new Print();
        int size = 10;
        double radius = 50;
        List<int[]> coord = new ArrayList<>();
        double R = 30.0;
        double r = 12.0;
        double angleX = Math.toRadians(45);
        double angleZ = Math.toRadians(30);
        double cosX = Math.cos(angleX), sinX = Math.sin(angleX);
        double cosZ = Math.cos(angleZ), sinZ = Math.sin(angleZ);
        for(int z = -(int)radius ; z <= (int)radius ; z++){
            for(int y = -(int)radius ; y <= (int)radius ; y++){
                for(int x = -(int)radius ; x <= (int)radius ; x++){
                    double y1 = y * cosX - z * sinX;
                    double z1 = y * sinX + z * cosX;
                    double x1 = x;               
                    double x2 = x1 * cosZ - y1 * sinZ;
                    double y2 = x1 * sinZ + y1 * cosZ;
                    double z2 = z1;                    
                    double distXY = Math.sqrt(x2*x2 + z2*z2) - R;
                    if (distXY*distXY + y2*y2 < r*r) {
                        coord.add(new int[]{x, y, z});
                    }
                }
            }
        }
        int[][] coords = new int[coord.size()][3];
        for(int i = 0; i < coord.size(); i++){
            coords[i] = coord.get(i);
        }
        int num = coords.length;
        System.out.print(num);
        List<cube> cubes = new ArrayList<>();
        for(int q = 0 ; q < num ; q++){
            cubes.add(new cube(size));
        }
        double[] cube = new double[cubes.size() * 108];
        display d = new display();
        double x1; double y1 ;double z1; double x2; double y2 ;double z2; double x3; double y3 ;double z3;
        double x;  double y;  double z;
        double zoom = 1; double fov = 90;
        double cam_x = 400;   double azimuthal_angle = 125;
        double cam_y =  -500;   double polar_angle = 135;
        double cam_z =  500;   double fov_factor = zoom/(Math.atan(Math.toRadians(fov)));
        double displace_x = size; //double theta = 0;
        double displace_y = size; //double phi = Math.toRadians(90);
        double displace_z = size; double rotation = 45;
        d.configure(cam_x, cam_y, cam_z, polar_angle, azimuthal_angle);
        azimuthal_angle = Math.toRadians(azimuthal_angle);
        polar_angle = Math.toRadians(polar_angle);
        rotation = Math.toRadians(rotation);
        int pixel_x = 1080;
        int pixel_y = 1080;
        double[] temp = new double[108];
        double avg_x;
        double avg_y;
        double avg_z;
        int p = 0;
        ArrayList<Double> data = new ArrayList<>();
        //System.out.println(" the triangles that will be printed are passed thru back face culling");
        for(int j =65; j<=65 ; j++){
            for(int q = 0 ; q < num ; q++){
                cubes.get(q).editcube(displace_x*coords[q][0], displace_y*coords[q][1], displace_z*coords[q][2]);
            }
            for(int k = 0 ; k<cubes.size() ; k++){
                temp = d.render(cubes.get(k).output).clone();
                for(int l = 0; l < 108 ; l++){
                    cube[108*k+l] = temp[l];
                }
            }
            for(int i = 0 ; i<12*cubes.size() ; i++){
            
            if(p%12 == 0){
                p = 0;
            }
            x1 = cube[9*i]    ; y1 = cube[9*i + 1]; z1 = cube[9*i + 2]; 
            x2 = cube[9*i + 3]; y2 = cube[9*i + 4]; z2 = cube[9*i + 5]; 
            x3 = cube[9*i + 6]; y3 = cube[9*i + 7]; z3 = cube[9*i + 8]; 
            x = (y2 - y1)*(z3 - z1) - (z2 - z1)*(y3 - y1);
            y = (x3 - x1)*(z2 - z1) - (x2 - x1)*(z3 - z1);
            z = (x2 - x1)*(y3 - y1) - (x3 - x1)*(y2 - y1); 
            avg_x = (x1 + x2 + x3)/3;
            avg_y = (y1 + y2 + y3)/3;
            avg_z = (z1 + z2 + z3)/3;
            if(-(x*avg_x + y*avg_y + z*avg_z) > 1e-4){
                double mag = Math.sqrt(x*x + y*y + z*z);//System.out.print("index " + i);
                data.add(y1*fov_factor/x1);//System.out.print(" x1: " + x1 + " y1: " + y1 + " z1: " + z1);
                data.add(z1*fov_factor/x1);//System.out.print(" x2: " + x2 + " y2: " + y2 + " z2: " + z2);
                data.add(y2*fov_factor/x2);//System.out.println(" x3: " + x3 + " y3: " + y3 + " z3: " + z3);
                data.add(z2*fov_factor/x2);
                data.add(y3*fov_factor/x3);
                data.add(z3*fov_factor/x3);//05
                data.add(y1);
                data.add(y2);
                data.add(y3);
                data.add(z1);
                data.add(z2);
                data.add(z3);//11
                data.add(x1);//12
                data.add(x2);
                data.add(x3);//14
                data.add(uv[6*p + 0]);//uvx1 15
                data.add(uv[6*p + 1]);//uvy1 16 
                data.add(uv[6*p + 2]);//uvx2 17 
                data.add(uv[6*p + 3]);//uvy2 18
                data.add(uv[6*p + 4]);//uvx3 19
                data.add(uv[6*p + 5]);//uvy3 20
                data.add(x/mag);//15
                data.add(y/mag);
                data.add(z/mag);
            }
            p++;
        }
        double[] arr = new double[data.size()];
        for(int i=0;i<data.size();i++){
            arr[i] = data.get(i);
        }
        P.print(pixel_x, pixel_y, arr);
        data.clear();
        }
    }
}
class delay{
    void Delay(int i){
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
class cube{
    double cube[] = new double[108];
    double[] uv = new double[72];
    double x = 0;
    double y = 0;
    double z = 0;
    int base = 3;
    double w;
    double u_x;
    double u_y;
    double u_z;
    double[] output = new double[108];

    cube(double a){
        double h = a/2;
        this.cube = new double[]{-h,-h, h,  h,-h, h,  h, h, h,  -h,-h, h,  h, h, h,  -h, h, h,   h,-h,-h, -h,-h,-h, -h, h,-h,   h,-h,-h, -h, h,-h,  h, h,-h,   h,-h, h,  h,-h,-h,  h, h,-h,   h,-h, h,  h, h,-h,  h, h, h,  -h,-h,-h, -h,-h, h, -h, h, h,  -h,-h,-h, -h, h, h, -h, h,-h,  -h, h, h,  h, h, h,  h, h,-h,  -h, h, h,  h, h,-h, -h, h,-h,  -h,-h,-h,  h,-h,-h,  h,-h, h,  -h,-h,-h,  h,-h, h, -h,-h, h,};
        this.uv = new double[]  {63, 31,    48, 31,   48, 16,   63, 31,    48, 16,   63, 16,     31, 31,  16, 31,   16, 16,     31, 31,  16, 16,    31, 16,    47, 31,   32, 31,   32, 16,    47, 31,   32, 16,   47, 16,   15, 31,   0, 31,    0, 16,     15, 31,   0, 16,    15, 16,    16, 0,     31, 0,    31, 15,   16, 0,     31, 15,   16, 15,   16, 32,    31, 32,   31, 47,   16, 32,    31, 47,  16, 47   };
    }
    void editcube(double rotation, double phi, double theta, double displace_x, double displace_y, double displace_z){                                       
        w = Math.cos(rotation/2);
        u_x = Math.sin(rotation/2)*Math.sin(theta)*Math.cos(phi);
        u_y = Math.sin(rotation/2)*Math.sin(theta)*Math.sin(phi);
        u_z = Math.sin(rotation/2)*Math.cos(theta);

        for(int i = 0 ; i<36 ; i++){
            x = this.cube[base*i];
            y = this.cube[base*i + 1];
            z = this.cube[base*i + 2];
            output[base*i]     = x*(1-2*(u_y*u_y + u_z*u_z)) + 2*y*(u_x*u_y - w*u_z) + 2*z*(u_x*u_z + w*u_y);
            output[base*i + 1] = 2*x*(u_x*u_y + w*u_z) + y*(1-2*(u_x*u_x + u_z*u_z)) + 2*z*(u_y*u_z - w*u_x);
            output[base*i + 2] = 2*x*(u_x*u_z - w*u_y) + 2*y*(u_y*u_z + w*u_x) + z*(1-2*(u_x*u_x + u_y*u_y));
        }
        for(int i = 0 ; i<36 ; i++){
            output[base*i] = output[base*i] + displace_x;
            output[base*i + 1] = output[base*i + 1] + displace_y;
            output[base*i + 2] = output[base*i + 2] + displace_z;
        }
    }
    void editcube(double displace_x, double displace_y, double displace_z){                                       
        for(int i = 0 ; i<36 ; i++){
            x = this.cube[base*i];
            y = this.cube[base*i + 1];
            z = this.cube[base*i + 2];
            output[base*i] =     x + displace_x;
            output[base*i + 1] = y + displace_y;
            output[base*i + 2] = z + displace_z;
        }
    }
} 
class display{
    quaternion rotate = new quaternion();
    double cam_x;
    double cam_y;
    double cam_z;
    double cam_i;
    double cam_j;
    double cam_k;
    double azimuthal_angle;
    double polar_angle;
    double[] list = new double[2];
    void configure(double cam_x, double cam_y ,double cam_z, double polar_angle,double azimuthal_angle){
        this.azimuthal_angle = Math.toRadians(azimuthal_angle);
        this.polar_angle = Math.toRadians(polar_angle);
        this.cam_i = Math.sin(this.polar_angle)*Math.cos(this.azimuthal_angle);
        this.cam_j = Math.sin(this.polar_angle)*Math.sin(this.azimuthal_angle);
        this.cam_k = Math.cos(this.polar_angle);
        this.cam_x = cam_x;
        this.cam_y = cam_y;
        this.cam_z = cam_z;
    }
    double x;
    double y;
    double p;
    double q;
    double r;
    double t;
    double X;
    double Y;
    double Z;
    double[] xyz = new double[3];
    double[] result = new double[108];
    double[] render(double[] cube){
        if(Math.abs(cam_i - 1) < 1e-9){
            for(int i = 0 ; i < 36 ; i++){
                result[3*i] = cube[3*i] - cam_x; result[3*i + 1] = cube[3*i + 1] - cam_y; result[3*i + 2] = cube[3*i + 2] - cam_z;
            } 
        }else if(Math.abs(cam_i + 1) < 1e-9){
           for(int i = 0 ; i < 36 ; i++){
                result[3*i] = cam_x - cube[3*i]; result[3*i + 1] = cube[3*i + 1] - cam_y; result[3*i + 2] = cube[3*i + 2] - cam_z;
            } 
        }else{
            for (int i = 0; i < 36; i++) {
                double px = cube[3*i]     - cam_x; double py = cube[3*i + 1] - cam_y; double pz = cube[3*i + 2] - cam_z;
                xyz = rotate.Quaternion(-azimuthal_angle, 0, 0, 1, px, py, pz).clone();
                xyz = rotate.Quaternion(Math.PI / 2 - polar_angle, 0, 1, 0, xyz[0], xyz[1], xyz[2]).clone();
                result[3*i]     = xyz[0]; result[3*i + 1] = xyz[1]; result[3*i + 2] = xyz[2];
            }
        }  

        return result;
    }
}
class quaternion{
    double w;
    double u_x;
    double u_y;
    double u_z;
    double mag;
    double[] xyz = new double[3];
    double[] Quaternion(double rotation, double i, double j, double k, double x, double y, double z){
        w = Math.cos(rotation/2);
        mag = Math.sqrt(i*i + j*j + k*k);
        if(mag < 1e-8){
            xyz[0]=x; xyz[1]=y; xyz[2]=z;
            return xyz;
        }
        u_x = (Math.sin(rotation/2)*i)/mag;
        u_y = (Math.sin(rotation/2)*j)/mag;
        u_z = (Math.sin(rotation/2)*k)/mag;
        xyz[0] = x*(1-2*(u_y*u_y + u_z*u_z)) + 2*y*(u_x*u_y - w*u_z) + 2*z*(u_x*u_z + w*u_y);
        xyz[1] = 2*x*(u_x*u_y + w*u_z) + y*(1-2*(u_x*u_x + u_z*u_z)) + 2*z*(u_y*u_z - w*u_x);
        xyz[2] = 2*x*(u_x*u_z - w*u_y) + 2*y*(u_y*u_z + w*u_x) + z*(1-2*(u_x*u_x + u_y*u_y));
        return xyz;
    }
}
class zbuffer{
    int l;
    double[] ijk = new double[3]; 
    double mag;
    double sign;
    double[] T;
    double[] Z;
    BufferedImage image = null;
    int length;
    zbuffer(double[] T){
        this.T = T;
        this.length = T.length/24; //  we send raw projected values of triangle along with depth of each point and normal vector
        try {
            File file = new File("texture.png");
            image = ImageIO.read(file);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    delay d = new delay();
    double reflection = 0;
    double x1,x2,x3,y1,y2,y3,denom,w1,w2,w3,z1,z2,z3;
    double PX; double X1; double X2; double X3;
    double PY; double Y1; double Y2; double Y3;
    double PZ; double Z1; double Z2; double Z3;
    double k = 40;
    int x_min;
    int x_max; 
    int y_min; 
    int y_max;
    BufferedImage Zbuffer(int pixel_x, int pixel_y){
        BufferedImage img = new BufferedImage(pixel_x, pixel_y, BufferedImage.TYPE_INT_RGB);
        l=-1;
        int pixel = 0xFF000000;
        double[] zbuff = new double[pixel_x*pixel_y];
        int[] triangle_index = new int[pixel_x*pixel_y];
        Arrays.fill(zbuff, Double.MAX_VALUE);
        Arrays.fill(triangle_index, -1);
        double comz;
        double scaling = pixel_y/2.0;
        for(int index = 0 ; index < length ; index++){
            x1 = T[index*24+0] * scaling + pixel_x/2.0; y1 = T[index*24+1] * scaling + pixel_y/2.0; z1 = T[index*24+12];
            x2 = T[index*24+2] * scaling + pixel_x/2.0; y2 = T[index*24+3] * scaling + pixel_y/2.0; z2 = T[index*24+13];
            x3 = T[index*24+4] * scaling + pixel_x/2.0; y3 = T[index*24+5] * scaling + pixel_y/2.0; z3 = T[index*24+14];
            x_min = (int)(Math.floor((Math.min(x1,Math.min(x2,x3))) - 1));
            x_max = (int)(Math.ceil ((Math.max(x1,Math.max(x2,x3))) + 1));
            y_min = (int)(Math.floor((Math.min(y1,Math.min(y2,y3))) - 1));
            y_max = (int)(Math.ceil ((Math.max(y1,Math.max(y2,y3))) + 1));
            if(x_min < 0) x_min = 0;
            if(x_max > pixel_x-1) x_max =  pixel_x-1;
            if(y_min < 0) y_min = 0;
            if(y_max > pixel_y-1) y_max =  pixel_y-1;
            denom = (x2 - x1)*(y3 - y2) - (x3 - x2)*(y2 - y1);
            if(Math.abs(denom) < 1e-9) continue;
            for(int py = y_min ; py < y_max ; py++){
                for(int px = x_min ; px < x_max ; px++){
                    comz = zbuff[py * pixel_x + px];
                    if(z1 > comz && z2 > comz && z3 > comz) continue;
                    w3 = ((x2 - x1)*(py - y1) - (y2 - y1)*(px - x1))/denom;
                    w1 = ((x3 - x2)*(py - y2) - (y3 - y2)*(px - x2))/denom;
                    w2 = 1 - (w3 + w1);
                    if(w1 >= -1e-8 && w2 >= -1e-8 && w3 >= -1e-8){
                        double interpolatedReciprocalZ = w1*(1.0/z1) + w2*(1.0/z2) + w3*(1.0/z3);
                        if(comz > 1/interpolatedReciprocalZ){
                            zbuff[py * pixel_x + px] = comz = 1/interpolatedReciprocalZ;
                            triangle_index[py*pixel_x + px] = index;
                        }
                    }
                }
            }
        }
        double k = 30;
        System.out.println("done");
        for(int py = 0 ; py < pixel_y ; py++){
            for(int px = 0 ; px < pixel_x ; px++){
                if(triangle_index[py*pixel_x + px] == -1){
                    continue ; 
                }
                int index = triangle_index[py*pixel_x + px];
                X1 = T[index*24+6];  X2 = T[index*24+7];  X3 = T[index*24+8];
                Y1 = T[index*24+9];  Y2 = T[index*24+10]; Y3 = T[index*24+11];
                Z1 = T[index*24+12]; Z2 = T[index*24+13]; Z3 = T[index*24+14];
                pixel = 0xFFFFFFFF;
                comz = zbuff[py * pixel_x + px];
                PX = ((px-pixel_x/2)*2.0/pixel_y)*comz; 
                PY = ((py-pixel_y/2)*2.0/pixel_y)*comz;
                PZ =   comz;
                //System.out.println("range ")
                double d00 = (X2 - X1)*(X2 - X1) + (Y2 - Y1)*(Y2 - Y1) + (Z2 - Z1)*(Z2 - Z1);
                double d01 = (X2 - X1)*(X3 - X1) + (Y2 - Y1)*(Y3 - Y1) + (Z2 - Z1)*(Z3 - Z1);
                double d11 = (X3 - X1)*(X3 - X1) + (Y3 - Y1)*(Y3 - Y1) + (Z3 - Z1)*(Z3 - Z1);
                double d20 = (PX - X1)*(X2 - X1) + (PY - Y1)*(Y2 - Y1) + (PZ - Z1)*(Z2 - Z1);
                double d21 = (PX - X1)*(X3 - X1) + (PY - Y1)*(Y3 - Y1) + (PZ - Z1)*(Z3 - Z1);
                double denom = d00*d11 - d01*d01;
                double W2 = (d11*d20 - d01*d21) / denom;
                double W3 = (d00*d21 - d01*d20) / denom;
                double W1 = 1.0 - W2 - W3;
                double u1 = (W1*T[index*24+15] + W2*T[index*24+17] + W3*T[index*24+19]);
                double v1 = (W1*T[index*24+16] + W2*T[index*24+18] + W3*T[index*24+20]);
                int u = (int)Math.floor(u1);
                int v = (int)Math.floor(v1);
                u = Math.min(63, u);
                u = Math.max(0 , u);            
                v = Math.min(47, v);
                v = Math.max(0 , v);
                pixel = image.getRGB(u, v);
                double mag = Math.sqrt(PX*PX + PY*PY + PZ*PZ);
                double attenuation = 1.0 / (1.0 + 0.08 * mag * mag);
                reflection = -((PZ*T[index*24+21] + PX*T[index*24+22] + PY*T[index*24+23])*attenuation/mag);
                reflection = k*0.01 + (1 - k*0.01)*reflection;
                reflection = Math.max(0.04, reflection);
                int a1 = ((pixel >> 24) & 0xFF);
                int r1 = ((pixel >> 16) & 0xFF);
                int g1 = ((pixel >> 8)  & 0xFF);
                int b1 = ( pixel & 0xFF);
                a1 = (int)(a1*reflection);
                r1 = (int)(r1*reflection);
                g1 = (int)(g1*reflection);
                b1 = (int)(b1*reflection);
                pixel = (a1 << 24) | (r1 << 16) | (g1 << 8) | (b1);
                img.setRGB(px,pixel_y -1 - py, pixel);
            }
        }
        return img;
    }
}
class DrawingCanvas extends JComponent{
    private int width;
    private int height;
    private double[] tri;
    BufferedImage img;
    public DrawingCanvas(int w, int h, double[] T){
        width = w;
        height = h;
        tri = T;
    }
    Graphics g2d;
    BufferedImage print(){
        zbuffer check = new zbuffer(tri);
        img = check.Zbuffer(width, height);
        System.out.println("done::::::::::::::::::::::::::");
        return img;
    }
    protected void paintComponent(Graphics g){
        zbuffer check = new zbuffer(tri);
        System.out.println("done::::::::::::::::::::::::::");
        g.drawImage(check.Zbuffer(width, height), 0, 0, null);
    }
}
class Print{
    void print(int w, int h, double[] T){
        JFrame f = new JFrame();
        DrawingCanvas dc = new DrawingCanvas(w, h, T);
        ImageIcon image = new ImageIcon("icon.png");
        //f.setResizable(false);
        f.setIconImage(image.getImage());
        f.setSize(w,h);
        f.setTitle("BIGMONKEYAK47");
        f.add(dc);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
