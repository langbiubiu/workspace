package ipaneltv.toolkit.media;

/**
 * lyre 20:58:31<br>
 * 框架的问题<br>
 * 客户端 player，服务端playerContext，Service，负责创建playerContext,并提供工具给playerContext使用，
 * 典型的比如PlayResourceScheduler，Widgetcontroller,<br>
 * 这么做的原因是，每个PlayerContext，都是为具体的针对性的Player存在的，并不会服务于其他的Player，换句话说，会创建很多的Player<br>
 * lyre 20:58:41<br>
 * 大的思路都是围绕这个展开的<br>
 * lyre 20:59:45<br>
 * 服务端这边将播放的Context和那些需要共享的资源分开了，Context里面只是使用资源，但不持有资源，Context，
 * 销毁资源还在Service这里记录着，比如DataHolding所查询到的数据库记录啥的<br>
 * lyre 21:00:51<br>
 * Context要变为当前的时候，就对所有要使用的资源进行reserve，都成功了，就可以使用，
 * 典型的就是PlayResourceScheduler里面创建的LivePlayState<br>
 * lyre 21:01:01<br>
 * 以及前面提到DataHoldings<br>
 * lyre 21:01:08<br>
 * Widgetcontroll<br>
 * lyre 21:01:34<br>
 * 所有需要在不同的Player之间共享的资源，都要这样来使用<br>
 * lyre 21:02:00<br>
 * 这样，系统有多个tuner，多player，多ca解扰，就可以很自然的运作起来<br>
 */
public interface ReserveStateInterface {
	boolean reserve();

	void loosen(boolean clearState);

	boolean isReserved();
	
	//void close();
}
