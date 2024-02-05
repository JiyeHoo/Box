# 说明
Fork 自 TvBox

内置了一些来自网络的接口：https://xn--sss604efuw.top/

资源来自这里，与本人无关，仅仅是收集。

新增了 讯飞 语音唤醒的功能，在设置开启之后，关键词“你好海豹”、“贾维斯”。
唤醒之后直接说出搜索的内容，也可以加前缀“我想看”、“播放”。

例如：我想看《三大队》



# 配置说明

=== Source Code - Editing the app default settings ===
/src/main/java/com/github/tvbox/osc/base/App.java

    private void initParams() {

        putDefault(HawkConfig.HOME_REC, 2);       // Home Rec 0=豆瓣, 1=推荐, 2=历史
        putDefault(HawkConfig.PLAY_TYPE, 1);      // Player   0=系统, 1=IJK, 2=Exo
        putDefault(HawkConfig.IJK_CODEC, "硬解码");// IJK Render 软解码, 硬解码
        putDefault(HawkConfig.HOME_SHOW_SOURCE, true);  // true=Show, false=Not show
        putDefault(HawkConfig.HOME_NUM, 2);       // History Number
        putDefault(HawkConfig.DOH_URL, 2);        // DNS
        putDefault(HawkConfig.SEARCH_VIEW, 2);    // Text or Picture

    }


如果你想使用讯飞语音，你需要安装官网 V5 的文档，替换 ivw 等唤醒文件，并替换你的 jar sdk，将 appid 改成你的

