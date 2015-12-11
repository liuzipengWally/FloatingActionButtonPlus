package com.lzp.floatingactionbuttonplusdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.lzp.floatingactionbuttonplus.FabTagLayout;
import com.lzp.floatingactionbuttonplus.FloatingActionButtonPlus;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<String> mList;
    private RecyclerView mRecyclerView;
    private FloatingActionButtonPlus mActionButtonPlus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
        events();
    }

    private void events() {
        mActionButtonPlus.setOnItemClickListener(new FloatingActionButtonPlus.OnItemClickListener() {
            @Override
            public void onItemClick(FabTagLayout tagView, int position) {
                Toast.makeText(MainActivity.this, "Click btn" + position, Toast.LENGTH_SHORT).show();
            }
        });

//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    if (dy > 0) {
//                        mActionButtonPlus.hideFab();
//                    } else {
//                        mActionButtonPlus.showFab();
//                    }
//                }
//            }
//        });
    }

    private void init() {
        mList = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            mList.add("this is item" + i);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new MyAdapter(this, mList));
        mActionButtonPlus = (FloatingActionButtonPlus) findViewById(R.id.FabPlus);
//        mActionButtonPlus.setContentIcon(getResources().getDrawable(R.mipmap.ic_get_app_white_48dp)); //设置主Fab的icon图标
//        mActionButtonPlus.setRotateValues(45); //设置主Fab被点击时旋转的度数，默认为45度
//        boolean state = mActionButtonPlus.getSwitchFabDisplayState();  //获取当前Fab的显示状态，显示时返回true，隐藏返回false
    }
}
