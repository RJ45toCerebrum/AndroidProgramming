package evolvevr.rajitut;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import org.rajawali3d.Object3D;
import org.rajawali3d.curves.CompoundCurve3D;
import org.rajawali3d.curves.ICurve3D;
import org.rajawali3d.curves.SVGPath;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;

import java.util.List;
import java.util.Stack;

/**
 * Created by Tyler on 2/26/2017.
 */

public class MyRenderer extends Renderer {

    private Context context;
    private DirectionalLight directionalLight;
    private Sphere earth;
    private Float[] rgb = {0f, 0.5f, 0.5f};
    Object3D plane;
    private Float[] planePos = {1.0f,1.0f};

    public MyRenderer(Context context){
        super(context);
        // renderer needs a context; this is actually android specific
        this.context = context;
        // set the frame rate of the rendere
        this.setFrameRate(60);
    }

    @Override
    public void onTouchEvent(MotionEvent event){
        Toast.makeText(context, "Touching", Toast.LENGTH_SHORT).show();
    }

    public void onOffsetsChanged(float x, float y, float w, float h, int i, int j){

    }

    public void SetPlanePos(float x, float y){
        planePos[0] = x * 0.005f;
        planePos[1] = -y * 0.002f;
        Toast.makeText(context, planePos[0].toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRender(final long elapsed, final double deltaTime){
        super.onRender(elapsed, deltaTime);
        earth.rotate(Vector3.Axis.Y, 1.0);
        directionalLight.setColor(rgb[0], rgb[1], rgb[2]);

        rgb[0] = (rgb[0] + 0.2f * (float)deltaTime) % 1;
        rgb[1] = (rgb[1] + 0.2f * (float)deltaTime) % 1;
        rgb[2] = (rgb[2] + 0.2f * (float)deltaTime) % 1;

        plane.setPosition(planePos[0],planePos[1],1);
    }


    public void initScene(){
        directionalLight = new DirectionalLight(1f, 0.2f, -1.0f);
        directionalLight.setColor(1.0f, 0.9f, 0.9f);
        directionalLight.setPower(1.5f);
        getCurrentScene().addLight(directionalLight);

        Material earthMat = new Material();
        earthMat.enableLighting(true);
        earthMat.setDiffuseMethod(new DiffuseMethod.Lambert());
        earthMat.setColor(0);

        plane = new Plane(Vector3.Axis.Z);
        plane.setMaterial(earthMat);
        plane.setPosition(0,1,1);

        try {
            SVGPath path = new SVGPath();
            String svg = "M89.171,155.172c2.668-0.742,4.23-3.531,3.475-6.238c-0.76-2.678-3.549-4.266-6.265-3.492\n" +
                    "        c-2.669,0.76-4.235,3.568-3.476,6.24C83.673,154.365,86.454,155.948,89.171,155.172z";
            List<CompoundCurve3D> paths = path.parseString(svg);
            Stack pathPoints = new Stack();

            for (int i = 0; i < paths.size(); i++) {
                ICurve3D subPath = paths.get(i);
                Stack<Vector3> points = new Stack<Vector3>();
                int subdiv = 1000;
                for (int j = 0; j <= subdiv; j++) {
                    Vector3 result = new Vector3();
                    subPath.calculatePoint(result, j / subdiv);
                    points.add(result);
                }

                pathPoints.add(points);
                Line3D line = new Line3D(points, 1);
                Material material = new Material();
                material.setColor(new float[]{0.5f, 0.5f, 0.5f});
                line.setMaterial(material);
                getCurrentScene().addChild(line);
            }
        }
        catch(Exception e){
            Log.i("Exception SVG", e.getMessage());
        }

        Texture earthText = new Texture("Earth", R.drawable.earthtruecolor_nasa_big);
        try {
            earthMat.addTexture(earthText);
            earth = new Sphere(1, 24, 24);
            earth.setMaterial(earthMat);
            getCurrentScene().addChild(earth);
            getCurrentScene().addChild(plane);
            //getCurrentScene().addChild(svg);
            getCurrentCamera().setZ(5.2f);
        }
        catch (Exception e){
            Log.e("Texture Exception", e.getMessage());
        }
    }
}
