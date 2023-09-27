package top.wxip.carlink.client;

import java.net.DatagramSocket;
import java.net.Socket;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GlobalItem {
    @Getter
    private static final GlobalItem instance = new GlobalItem();

    private String remoteAddr;

    private Socket videoSocket;
    private DatagramSocket controlSocket;
    private Socket audioSocket;
}
