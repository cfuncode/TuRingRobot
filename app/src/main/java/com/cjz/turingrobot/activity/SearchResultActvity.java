package com.cjz.turingrobot.activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cjz.turingrobot.R;
import com.cjz.turingrobot.adapter.ChatAdapter;
import com.cjz.turingrobot.db.ChatBean;
import com.cjz.turingrobot.popuplist.PopupList;
import com.github.zackratos.ultimatebar.UltimateBar;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static org.litepal.LitePalApplication.getContext;

@SuppressLint("SetTextI18n")
public class SearchResultActvity extends AppCompatActivity {
    private static final String TAG = "SearchResultActvity";
    private TextView tv_search_result;
    private List<ChatBean> chatBeanList;
    private ChatAdapter adapter;
    private ListView mList;
    private Button mAllSelect;
    private Button mAllNotSelect;
    private Button mBackSelect;
    private Button mDelete;
    private LinearLayout mToolsBar;
    private List<String> popupMenuItemList = new ArrayList<>();
    private String queryString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明化状态栏、导航栏、沉浸式状态栏
        UltimateBar.Companion.with(this).statusDark(true).applyNavigation(true).create().immersionBar();
        setContentView(R.layout.activity_search_result);
        RobotActivity.isReturn=true;
        // 从布局文件中获取名叫tl_result的工具栏
        Toolbar tl_result = findViewById(R.id.tl_result);
        // 设置工具栏的背景
        tl_result.setBackgroundResource(R.color.blue);
        // 设置工具栏的标志图片
//        tl_result.setLogo(R.mipmap.robot_icon);
        // 设置工具栏的标题文字
        tl_result.setTitle("历史记录");
        // 设置工具栏的导航图标
        tl_result.setNavigationIcon(R.drawable.ic_back);
        // 使用tl_result替换系统自带的ActionBar
        setSupportActionBar(tl_result);
        tv_search_result = findViewById(R.id.tv_search_result);
        // 执行搜索查询操作
        mList = findViewById(R.id.list);
        mAllSelect = findViewById(R.id.all_select);
        mAllSelect.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                btnSelectAllList();
                startCircular(mAllSelect);
            }
        });
        mAllNotSelect = findViewById(R.id.all_not_select);
        mAllNotSelect.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                btnNoList();
                startCircular(mAllNotSelect);
            }
        });
        mBackSelect = findViewById(R.id.back_select);
        mBackSelect.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                btnfanxuanList();
                startCircular(mBackSelect);
            }
        });
        mDelete = findViewById(R.id.delete);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                btnOperateList();
                startCircular(mDelete);
            }
        });
        mToolsBar = findViewById(R.id.tools_bar);
        popupMenuItemList.add(getString(R.string.copy));
        popupMenuItemList.add(getString(R.string.delete));
        popupMenuItemList.add(getString(R.string.multi_choice));
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(RobotActivity.this, "onItemClicked:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                PopupList popupList = new PopupList(view.getContext());
                popupList.showPopupListWindow(view, position, location[0] + view.getWidth() / 2,
                        location[1], popupMenuItemList, new PopupList.PopupListListener() {
                            @Override
                            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                                if (!adapter.flag) { //不是编辑状态，弹出菜单
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public void onPopupListClick(View contextView, int contextPosition, int position) {
                                switch (position) {
                                    case 0:
                                        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                        cm.setText(chatBeanList.get(contextPosition).getMessage());
                                        Toast.makeText(getContext(), "已复制到剪切板，快去粘贴吧~", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        mList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                                        ChatBean chatBean = chatBeanList.get(contextPosition);
                                        chatBeanList.remove(chatBean);
                                        adapter.notifyDataSetChanged();    //更新ListView列表
                                        tv_search_result.setText("有关：\"" + queryString + "\"的历史记录。（共" + chatBeanList.size() + "条）");
                                        LitePal.delete(ChatBean.class, chatBean.getId());
                                        break;
                                    case 2:
                                        chatBeanList.get(contextPosition).isCheck = true;
                                        btnEditList();
                                        break;
                                }
                            }
                        });
                return true;
            }
        });
        doSearchQuery(getIntent());
    }

    // 解析搜索请求页面传来的搜索信息，并据此执行搜索查询操作
    private void doSearchQuery(Intent intent) {
        if (intent != null) {
            // 如果是通过ACTION_SEARCH来调用，即为搜索框来源
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                // 获取额外的搜索数据
                Bundle bundle = intent.getBundleExtra(SearchManager.APP_DATA);
                chatBeanList = (List<ChatBean>) bundle.getSerializable("chatBeanList");
                // 获取实际的搜索文本
                queryString = intent.getStringExtra(SearchManager.QUERY);
                tv_search_result.setText("有关：\"" + queryString + "\"的历史记录。（共" + chatBeanList.size() + "条）");
                if (chatBeanList == null) return;
                adapter = new ChatAdapter(chatBeanList, this);
                mList.setAdapter(adapter);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 从menu_null.xml中构建菜单界面布局
        getMenuInflater().inflate(R.menu.menu_null, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // 点击了工具栏左边的返回箭头
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 编辑、取消编辑
     */
    public void btnEditList() {
        mList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        adapter.flag = !adapter.flag;
        if (adapter.flag) {
            mToolsBar.setVisibility(View.VISIBLE);
        } else {
            mToolsBar.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 全选
     */
    public void btnSelectAllList() {
        mList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        if (adapter.flag) {
            for (int i = 0; i < chatBeanList.size(); i++) {
                chatBeanList.get(i).isCheck = true;
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 全不选
     */
    public void btnNoList() {
        mList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        if (adapter.flag) {
            for (int i = 0; i < chatBeanList.size(); i++) {
                chatBeanList.get(i).isCheck = false;
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 反选
     */
    public void btnfanxuanList() {
        mList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        if (adapter.flag) {
            for (int i = 0; i < chatBeanList.size(); i++) {
                if (chatBeanList.get(i).isCheck) {
                    chatBeanList.get(i).isCheck = false;
                } else {
                    chatBeanList.get(i).isCheck = true;
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取选中数据
     */
    public void btnOperateList() {
        mList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        final List<ChatBean> ids = new ArrayList<>();

        if (adapter.flag) {

            for (int i = 0; i < chatBeanList.size(); i++) {
                if (chatBeanList.get(i).isCheck) {
                    ids.add(chatBeanList.get(i));
                }
            }
        }
        if (ids.size() > 0) {
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("是否删除选中的记录？（共" + ids.size() + "条）")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (ChatBean chatBean : ids) {
                                chatBeanList.remove(chatBean);
                                LitePal.delete(ChatBean.class, chatBean.getId());
                            }
                            adapter.notifyDataSetChanged();
                            tv_search_result.setText("有关：\"" + queryString + "\"的历史记录。（共" + chatBeanList.size() + "条）");
                            Toast.makeText(SearchResultActvity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            btnEditList();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            dialog = builder.show();
        } else {
            Toast.makeText(this, "您选择的记录为空", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startCircular(Button myView) {
        // get the center for the clipping circle
        int cx = (myView.getLeft() + myView.getRight()) / 2;
        int cy = (myView.getTop() + myView.getBottom()) / 2;

        // get the final radius for the clipping circle
        int dx = Math.max(cx, myView.getWidth() - cx);
        int dy = Math.max(cy, myView.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        // Android native animator
        Animator animator = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(200);
        animator.start();
    }

}
