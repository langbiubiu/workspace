package ipaneltv.toolkit.play;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;

/** 可在主线程直接使用接口(接口实现对调用耗时的一律转为消息异步处理) */
public interface PlayInterface {
    
    /** 选择频道播放 */
    void select ( long freq, int fflags, int program, int pflags );
    
    /** 选择播放 */
    void select ( final String furi, final int fflags, final String puri, final int pflags );
    
    /** 设置视频的显示范围 */
    void setDisplay ( int x, int y, int w, int h );
    
    /** 设置音量 */
    void setVolume ( float v );
    
    /**
     * 比对家长密码。
     * 
     * @param pwd
     */
    void checkPassword ( String pwd );
    
    /** 设置时移,第一次如果传入-1表示从当前时间开始,0则从起始时间开始 */
    void shift ( String uri, int offsetOfShiftStartTime, int flags );
    
    /** 时移seek或快进快退 */
    void shiftSeek ( long t );
    
    /** 停止时移 */
    void shiftStop ();
    
    /** 暂停时移 */
    void shiftPause ( String uri );
    
    /** 设置节目播放的标志 */
    void setProgramFlags ( int flags );
    
    /** 关注节目指南数据，focusTime距此时间近则优先处理,0表示不关注时间 */
    void observeProgramGuide ( ChannelKey ch, long focusTime );
    
    /** 关注的节pf节目信息 */
    void getPresentAndFollow ( ChannelKey ch );
    
    void syncSignalStatus ();
    
    public void loosenAllSession ();
}