package vmax.hsedorms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class ByeByeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bye_bye);

        TextView byebye_bots = (TextView) findViewById(R.id.byebye_bots);
        byebye_bots.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
