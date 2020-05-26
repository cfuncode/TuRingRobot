package com.cjz.turingrobot.activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.cjz.turingrobot.R;
import com.cjz.turingrobot.adapter.ChatAdapter;
import com.cjz.turingrobot.db.ChatBean;
import com.cjz.turingrobot.popuplist.PopupList;
import com.cjz.turingrobot.util.MenuUtil;
import com.cjz.turingrobot.util.MyOk;
import com.github.zackratos.ultimatebar.UltimateBar;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class RobotActivity extends BaseActivity {
    private static final String TAG = "RobotActivity";
    private ListView listView;
    private ChatAdapter adapter;
    private List<ChatBean> chatBeanList; //存放所有聊天数据的集合
    private EditText et_send_msg;
    private Button btn_send;
    //接口地址
    private static final String WEB_SITE = "http://openapi.tuling123.com/openapi/api/v2";
    private static final String USERID = "623125";
    private static final String KEY = "d1903503f2304b2ca3883f38b2a0238e";
    //POST请求体
    private static final String sendJson = "{\"reqType\": 0,\"perception\": {\"inputText\": {\"text\": \"%s\"},\"inputImage\": {\"url\": \"imageUrl\"},\"selfInfo\": {\"location\": {\"city\": \"北京\",\"province\": \"北京\",\"street\": \"信息路\"}}},\"userInfo\": {\"apiKey\": \"" + KEY + "\",\"userId\": \"" + USERID + "\"}}";
    private RelativeLayout mRlBottom;
    private Button mAllSelect;
    private Button mAllNotSelect;
    private Button mBackSelect;
    private Button mDelete;
    private LinearLayout mToolsBar;

    private String getSendJson(String sendMsg) {
        return String.format(sendJson, sendMsg);
    }

    private String sendMsg;    //发送的信息
    private String welcome[];  //存储欢迎信息

    private List<String> popupMenuItemList = new ArrayList<>();

    private SearchView.SearchAutoComplete sac_key; // 声明一个搜索自动完成的编辑框对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明化状态栏、导航栏、沉浸式状态栏
        UltimateBar.Companion.with(this).statusDark(true).applyNavigation(true).create().immersionBar();
        setContentView(R.layout.activity_robot);
        // 从布局文件中获取名叫tl_head的工具栏
        Toolbar tl_head = findViewById(R.id.tl_head);
        // 设置工具栏的标题文字
        tl_head.setTitle("机器人");
        // 使用tl_head替换系统自带的ActionBar
        setSupportActionBar(tl_head);
        chatBeanList = new ArrayList<ChatBean>();
        //获取内置的欢迎信息
        welcome = getResources().getStringArray(R.array.welcome);
        initView(); //初始化界面控件
    }

    public void initView() {
        listView = (ListView) findViewById(R.id.list);
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        et_send_msg = (EditText) findViewById(R.id.et_send_msg);
        btn_send = (Button) findViewById(R.id.btn_send);
        mRlBottom = findViewById(R.id.rl_bottom);
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
        adapter = new ChatAdapter(chatBeanList, this);
        listView.setAdapter(adapter);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendData();//点击发送按钮，发送信息
            }
        });
        et_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
            }
        });
        et_send_msg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendData();//点击Enter键也可以发送信息
                }
                return false;
            }
        });
        int position = (int) (Math.random() * welcome.length - 1); //获取一个随机数
        List<ChatBean> historys = LitePal.findAll(ChatBean.class);
        if (historys.size() > 0) {
            for (ChatBean history : historys) {
                chatBeanList.add(history);  //将机器人发送的信息添加到chatBeanList集合中
                adapter.notifyDataSetChanged();
            }
        } else {
            showData(welcome[position]); //用随机数获取机器人的首次聊天信息
        }

        popupMenuItemList.add(getString(R.string.copy));
        popupMenuItemList.add(getString(R.string.delete));
        popupMenuItemList.add(getString(R.string.multi_choice));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(RobotActivity.this, "onItemClicked:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
                                        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                                        ChatBean chatBean = chatBeanList.get(contextPosition);
                                        chatBeanList.remove(chatBean);
                                        adapter.notifyDataSetChanged();    //更新ListView列表
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
    }

    private void sendData() {
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        sendMsg = et_send_msg.getText().toString(); //获取你输入的信息
        if (TextUtils.isEmpty(sendMsg)) {             //判断是否为空
            Toast.makeText(this, "您还未输任何信息哦", Toast.LENGTH_LONG).show();
            return;
        }
        et_send_msg.setText("");
        //替换空格和换行
        sendMsg = sendMsg.replaceAll(" ", "").replaceAll("\n", "").trim();
        ChatBean chatBean = new ChatBean();
        chatBean.setMessage(sendMsg);
        chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
        chatBean.save();
        chatBeanList.add(chatBean);        //将发送的信息添加到chatBeanList集合中
        adapter.notifyDataSetChanged();    //更新ListView列表
        getDataFromServer();                //从服务器获取机器人发送的信息
    }

    private void getDataFromServer() {
        MyOk.post(WEB_SITE, getSendJson(sendMsg), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showData("主人，你的网络不好哦");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    final int code = jsonObject.getJSONObject("intent").getInt("code");
                    final String content = jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("values").getString("text");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateView(code, content);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showData(String message) {
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        ChatBean chatBean = new ChatBean();
        chatBean.setMessage(message);
        chatBean.setState(ChatBean.RECEIVE);//RECEIVE表示接收到机器人发送的信息
        chatBean.save();
        chatBeanList.add(chatBean);  //将机器人发送的信息添加到chatBeanList集合中
        adapter.notifyDataSetChanged();
    }

    private void updateView(int code, String content) {
        //code有很多种状，在此只例举几种，如果想了解更多，请参考官网http://www.tuling123.com
        switch (code) {
            case 4004:
                showData("主人，今天我累了，我要休息了，明天再来找我耍吧");
                break;
            case 40005:
                showData("主人，你说的是外星语吗？");
                break;
            case 40006:
                showData("主人，我今天要去约会哦，暂不接客啦");
                break;
            case 40007:
                showData("主人，明天再和你耍啦，我生病了，呜呜......");
                break;
            default:
                showData(content);
                break;
        }
    }

    protected long exitTime;//记录第一次点击时的时间

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mToolsBar.getVisibility() == View.VISIBLE) {
                btnEditList();
                for (ChatBean chatBean : chatBeanList) {
                    chatBean.isCheck = false;
                }
            } else if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(RobotActivity.this, "再按一次退出智能聊天程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                RobotActivity.this.finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 编辑、取消编辑
     */
    public void btnEditList() {
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
        adapter.flag = !adapter.flag;
        if (adapter.flag) {
            mToolsBar.setVisibility(View.VISIBLE);
            mRlBottom.setVisibility(View.GONE);
        } else {
            mToolsBar.setVisibility(View.GONE);
            mRlBottom.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 全选
     */
    public void btnSelectAllList() {
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
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
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
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
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
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
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
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
                    .setMessage("是否删除选中的记录？（共"+ids.size()+"条）")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (ChatBean chatBean : ids) {
                                chatBeanList.remove(chatBean);
                                LitePal.delete(ChatBean.class, chatBean.getId());
                            }
                            adapter.notifyDataSetChanged();
                            Toast.makeText(RobotActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
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

    private static boolean isSearch=false;
    // 根据菜单项初始化搜索框
    @SuppressLint("RestrictedApi")
    private void initSearchView(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        // 从菜单项中获取搜索框对象
        SearchView searchView = (SearchView) menuItem.getActionView();
        // 设置搜索框默认自动缩小为图标
        searchView.setIconifiedByDefault(getIntent().getBooleanExtra("collapse", true));
//        searchView.setIconifiedByDefault(false);1
        // 设置是否显示搜索按钮。搜索按钮只显示一个箭头图标，Android暂不支持显示文本。
        // 查看Android源码，搜索按钮用的控件是ImageView，所以只能显示图标不能显示文字。
        searchView.setSubmitButtonEnabled(true);
        // 从系统服务中获取搜索管理器
        SearchManager sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // 创建搜索结果页面的组件名称对象
        ComponentName cn = new ComponentName(this, SearchResultActvity.class);
        // 从结果页面注册的activity节点获取相关搜索信息，即searchable.xml定义的搜索控件
        SearchableInfo info = sm.getSearchableInfo(cn);
        if (info == null) {
            Log.d(TAG, "Fail to get SearchResultActvity.");
            return;
        }
        // 设置搜索框的可搜索信息
        searchView.setSearchableInfo(info);
        // 从搜索框中获取名叫search_src_text的自动完成编辑框
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                mRlBottom.setVisibility(View.GONE);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mRlBottom.setVisibility(View.VISIBLE);
                return false;
            }
        });
        sac_key = searchView.findViewById(R.id.search_src_text);
        // 设置自动完成编辑框的文本颜色
        sac_key.setTextColor(Color.WHITE);
        // 设置自动完成编辑框的提示文本颜色
        sac_key.setHintTextColor(Color.WHITE);
        // 给搜索框设置文本变化监听器
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 搜索关键词完成输入
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 搜索关键词发生变化
            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(RobotActivity.this, "文本改变了", Toast.LENGTH_SHORT).show();
                if (newText.length()>0){
                    chatBeanList2.clear();
                    for (ChatBean chatBean : chatBeanList) {
                        if (chatBean.getMessage().contains(newText)){
                            chatBeanList2.add(chatBean);
                        }
                    }
//                    doSearch(newText);
                }
                return true;
            }
        });
        Bundle bundle = new Bundle(); // 创建一个新包裹
        bundle.putSerializable("chatBeanList", (Serializable) chatBeanList2); // 往包裹中存放chatBeanList2
        // 设置搜索框的额外搜索数据
        searchView.setAppSearchData(bundle);
    }
    private List<ChatBean> chatBeanList2 = new ArrayList<>();

    // 自动匹配相关的关键词列表
    private void doSearch(String text) {
        if (chatBeanList.size()<=0)return;
        String[] hintArray = new String[chatBeanList.size()];
        for (int i = 0; i < chatBeanList.size(); i++) {
            if (chatBeanList.get(i).getMessage().contains(text)){
                hintArray[i]=chatBeanList.get(i).getMessage();
            }
        }
        // 根据提示词数组构建一个数组适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RobotActivity.this, R.layout.search_list_auto, hintArray);
        // 设置自动完成编辑框的数组适配器
        sac_key.setAdapter(adapter);
        // 给自动完成编辑框设置列表项的点击监听器
        sac_key.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // 一旦点击关键词匹配列表中的某一项，就触发点击监听器的onItemClick方法
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sac_key.setText(((TextView) view).getText());
            }
        });
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        // 显示菜单项左侧的图标
        MenuUtil.setOverflowIconVisible(featureId, menu);
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 从menu_search.xml中构建菜单界面布局
        getMenuInflater().inflate(R.menu.menu_search, menu);
        // 初始化搜索框
        initSearchView(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) { // 点击了工具栏左边的返回箭头
            finish();
        }else if (id == R.id.menu_refresh) { // 点击了刷新图标
            listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.menu_about) { // 点击了关于菜单项
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("作者：Click")
                    .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            dialog=builder.show();
            return true;
        } else if (id == R.id.menu_quit) { // 点击了退出菜单项
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isReturn){
            sac_key.setText("");
            adapter.flag=false;
            mToolsBar.setVisibility(View.GONE);
            mRlBottom.setVisibility(View.VISIBLE);
            listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
            chatBeanList=LitePal.findAll(ChatBean.class);
            adapter = new ChatAdapter(chatBeanList,this);
            listView.setAdapter(adapter);
            isReturn=false;
        }
    }
    public static boolean isReturn=false;
}
