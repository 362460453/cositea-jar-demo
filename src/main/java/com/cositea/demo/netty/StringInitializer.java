package  com.cositea.demo.netty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.cositea.demo.netty.coder.ByteArrayNewDecoder;
import com.cositea.demo.netty.handler.ServerHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * TODO:接收通道初始化(转成String)
 * @author liuyicong
 * @Date 2018年11月26日 上午10:03:31
 */
@Component
@Qualifier("stringInitializer")
public class StringInitializer extends ChannelInitializer<SocketChannel> {  
    
    @Autowired  
    ByteArrayNewDecoder byteArrayNewDecoder;  
  
	@Autowired  
    ByteArrayEncoder byteArrayEncoder;  
  
    @Autowired  
    ServerHandler serverHandler;  
  
    @Override  
    protected void initChannel(SocketChannel ch) throws Exception {  
        ChannelPipeline pipeline = ch.pipeline();  
        pipeline.addLast("decoder", byteArrayNewDecoder);  
        pipeline.addLast("handler", serverHandler);  
        pipeline.addLast("encoder", byteArrayEncoder);  
        pipeline.addLast(new LineBasedFrameDecoder(1024));
    }  

    public ByteArrayNewDecoder getByteArrayDecoder() {
 		return byteArrayNewDecoder;
 	}

 	public void setByteArrayDecoder(ByteArrayNewDecoder byteArrayNewDecoder) {
 		this.byteArrayNewDecoder = byteArrayNewDecoder;
 	}
    public ByteArrayEncoder getByteArrayEncoder() {
        return byteArrayEncoder;
    }

    public void setByteArrayEncoder(ByteArrayEncoder byteArrayEncoder) {
        this.byteArrayEncoder = byteArrayEncoder;
    }

    public ServerHandler getServerHandler() {  
        return serverHandler;  
    }  
  
    public void setServerHandler(ServerHandler serverHandler) {  
        this.serverHandler = serverHandler;  
    }  
  
}  
