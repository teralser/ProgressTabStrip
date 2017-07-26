package widget.com.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.CompoundButton;

import teralser.widget.ProgressTabStrip;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ProgressTabStrip pts = (ProgressTabStrip) findViewById(R.id.pts);
        pts.setPointsCount(2);

        ((AppCompatCheckBox) findViewById(R.id.holdEnabler))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        pts.setHoldPassedPositions(b);
                    }
                });

        ((AppCompatCheckBox) findViewById(R.id.animationEnabler))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        pts.setAnimated(b);
                    }
                });

        ((AppCompatCheckBox) findViewById(R.id.wrapEnabler))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        pts.setStripWidth(b ? 280 : 0);
                    }
                });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pts.getSelectedPosition() > 0) pts.setSelected(pts.getSelectedPosition() - 1);
            }
        });

        findViewById(R.id.forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pts.getSelectedPosition() != pts.getPointsCount() - 1)
                    pts.setSelected(pts.getSelectedPosition() + 1);
            }
        });
    }
}
