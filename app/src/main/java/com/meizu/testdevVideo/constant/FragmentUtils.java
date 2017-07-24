package com.meizu.testdevVideo.constant;

/**
 * Fragment类型
 * Created by maxueming on 2017/5/25.
 */

public class FragmentUtils {

    // 传入的Fragment类型
    public static final String FRAGMENT_TYPE = "fragment_type";

    public static final String UPDATE_APP_FRAGMENT = "业务更新";
    public static final String CHOOSE_APP_FRAGMENT = "应用选择";

    public enum FragmentType{

        UPDATE_APP(UPDATE_APP_FRAGMENT), CHOOSE_APP(CHOOSE_APP_FRAGMENT);

        private String fragmentName;

        FragmentType(String fragmentName) {
            this.fragmentName = fragmentName;
        }

        public String getFragmentName() {
            return fragmentName;
        }
    }

}
