package evolvevr.rajitut;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;


public class MainActivity extends AppCompatActivity
{
    private MyRenderer renderer;
    float touched_x, touched_y;
    boolean touched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView view = new SurfaceView(this);
        view.setFrameRate(60);
        view.setRenderMode(ISurface.RENDERMODE_WHEN_DIRTY);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                touched_x = event.getRawX();
                touched_y = event.getRawY();


                int action = event.getAction();
                switch(action){
                    case MotionEvent.ACTION_DOWN:
                        touched = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        touched = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        touched = false;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        touched = false;
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                        touched = false;
                        break;
                    default:
                }

                renderer.SetPlanePos(touched_x, touched_y);

                return true; //processed
            }
        });

        addContentView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));
        renderer = new MyRenderer(this);
        view.setSurfaceRenderer(renderer);
    }
}
