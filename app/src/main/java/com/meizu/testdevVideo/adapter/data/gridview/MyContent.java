package com.meizu.testdevVideo.adapter.data.gridview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成Adapter
 */
public class MyContent {
    public List<Map<String, Object>> ITEMS =new ArrayList<Map<String, Object>>();
    public Map<String, Object> MAP =new HashMap<String, Object>();

//    static {
//        // Add 3 sample items.
//        addItem(new DummyItem("DNS切换", R.mipmap.ic_dns));
//        addItem(new DummyItem("录制视频", R.mipmap.ic_record));
//        addItem(new DummyItem("脚本执行", R.mipmap.ic_uiautomator));
//        addItem(new DummyItem("应用信息", R.mipmap.ic_applist));
//    }

    /**
     * 新增列表
     * @param item
     */
    public void addItem(DummyItem item) {
        MAP =new HashMap<String, Object>();
        MAP.put("text", item.content);
        MAP.put("img", item.object);
        ITEMS.add(MAP);
    }


    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String content;
        public Object object;

        public DummyItem(String content, Object object) {
            this.content = content;
            this.object = object;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
