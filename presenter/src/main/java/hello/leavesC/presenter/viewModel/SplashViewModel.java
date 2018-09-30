package hello.leavesC.presenter.viewModel;

import android.app.Application;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMUserConfig;

import hello.leavesC.presenter.event.FriendEvent;
import hello.leavesC.presenter.event.GroupEvent;
import hello.leavesC.presenter.event.MessageEvent;
import hello.leavesC.presenter.event.RefreshEvent;
import hello.leavesC.presenter.event.SplashEvent;
import hello.leavesC.presenter.log.Logger;
import hello.leavesC.presenter.viewModel.base.BaseAndroidViewModel;
import hello.leavesC.sdk.Constants;
import tencent.tls.platform.TLSLoginHelper;
import tencent.tls.platform.TLSUserInfo;

/**
 * 作者：叶应是叶
 * 时间：2018/9/28 21:49
 * 描述：
 */
public class SplashViewModel extends BaseAndroidViewModel {

    private static final String TAG = "SplashViewModel";

    private MediatorLiveData<SplashEvent> eventLiveData = new MediatorLiveData<>();

    private TLSLoginHelper loginHelper;

    public SplashViewModel(@NonNull Application application) {
        super(application);
        loginHelper = TLSLoginHelper.getInstance().init(application,
                Constants.SDK_APP_ID, Constants.ACCOUNT_TYPE, Constants.APP_VERSION);
    }

    public void start() {
        TLSUserInfo lastUserInfo = loginHelper.getLastUserInfo();
        if (lastUserInfo == null) {
            eventLiveData.setValue(new SplashEvent(SplashEvent.LOGIN_OR_REGISTER));
            return;
        }
        String identifier = lastUserInfo.identifier;
        if (TextUtils.isEmpty(identifier)) {
            eventLiveData.setValue(new SplashEvent(SplashEvent.LOGIN_OR_REGISTER));
            return;
        }
        if (loginHelper.needLogin(identifier)) {
            SplashEvent splashEvent = new SplashEvent(SplashEvent.NAV_TO_LOGIN);
            splashEvent.setIdentifier(identifier);
            eventLiveData.setValue(splashEvent);
        } else {
            loginImServer(identifier);
        }
    }

    private void loginImServer(String identifier) {
        //登录之前要先初始化群和好友关系链缓存
        TIMUserConfig userConfig = new TIMUserConfig();
        userConfig = FriendEvent.getInstance().init(userConfig);
        userConfig = GroupEvent.getInstance().init(userConfig);
        userConfig = MessageEvent.getInstance().init(userConfig);
        userConfig = RefreshEvent.getInstance().init(userConfig);
        TIMManager.getInstance().setUserConfig(userConfig);
        TIMManager.getInstance().login(identifier, loginHelper.getUserSig(identifier), new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                showToast(s);
                eventLiveData.setValue(new SplashEvent(SplashEvent.LOGIN_OR_REGISTER));
            }

            @Override
            public void onSuccess() {
                eventLiveData.setValue(new SplashEvent(SplashEvent.LOGIN_SUCCESS));
                Logger.e(TAG, "onSuccess");
            }
        });
    }

    public String getLastUserIdentifier() {
        TLSUserInfo userInfo = loginHelper.getLastUserInfo();
        if (userInfo != null) {
            return userInfo.identifier;
        }
        return null;
    }

    public MediatorLiveData<SplashEvent> getEventLiveData() {
        return eventLiveData;
    }

}