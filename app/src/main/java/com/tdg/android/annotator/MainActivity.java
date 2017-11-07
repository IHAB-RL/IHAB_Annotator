package com.tdg.android.annotator;

import android.content.pm.ActivityInfo;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements Communicator{

    private String LOG = "MainActivity";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public NoScrollViewPager mViewPager;
    private AnnotationKeeper annotationKeeper;
    private FileWriter fileWriter;
    private boolean wasTouched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (NoScrollViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(3);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        annotationKeeper = new AnnotationKeeper(this);
        fileWriter = new FileWriter();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if ((position == 2) && (wasTouched)) {
                    annotationKeeper.setTimeEnd();
                    Log.i(LOG, "Endzeitpunkt wurde gesetzt.");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onResume() {
        setImmersive();
        super.onResume();
    }

    private void setImmersive() {
        mViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void setupViewPager(ViewPager viewPager) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFragment(new FragmentNewSession(),
                getResources().getString(R.string.tab_New_Session));
        mSectionsPagerAdapter.addFragment(new FragmentAnnotation(),
                getResources().getString(R.string.tab_Annotation));
        mSectionsPagerAdapter.addFragment(new FragmentFinalise(),
                getResources().getString(R.string.tab_Finalise));
        viewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void beginAnnotation(String raterId, String subjectId, boolean isUebung) {
        annotationKeeper.reset();
        annotationKeeper.setRaterID(raterId);
        annotationKeeper.setSubjectID(subjectId);
        annotationKeeper.setUebung(isUebung);
        annotationKeeper.setTimeStart();
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void finishAnnotation(String results, String freitext) {
        if (wasTouched) {
            annotationKeeper.setAdditionalData(results);
            annotationKeeper.setFreiText(freitext);
            fileWriter.saveToFile(this, annotationKeeper.getFileName(),
                    annotationKeeper.flushResults());
            annotationKeeper.reset();
            ((FragmentNewSession) mSectionsPagerAdapter.getFragmentFromPos(0)).reset();
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void addAnnotation(int code) {
        if (wasTouched) {
            annotationKeeper.addAnnotation(code);
            Log.i(LOG, "New annotation [" + annotationKeeper.getNumberOfAnnotations() + "]: " + code);
        }
    }

    @Override
    public void removeLastAnnotation() {
        if (wasTouched) {
            annotationKeeper.removeLastAnnotation();
            Log.i(LOG, "Annotation removed [" + annotationKeeper.getNumberOfAnnotations() + "]");
        }
    }

    @Override
    public void onBackPressed() {
        // Back button disabled
    }

    @Override
    public void setWasTouched(boolean touched) {
        wasTouched = touched;
        ((FragmentFinalise) mSectionsPagerAdapter.getFragmentFromPos(2)).setTouched(wasTouched);
    }

    @Override
    public void setImmersiveMode() {
        setImmersive();
    }
}