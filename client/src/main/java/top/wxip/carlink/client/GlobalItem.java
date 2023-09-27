package top.wxip.carlink.client;

import java.net.Socket;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GlobalItem {
    @Getter
    private static final GlobalItem instance = new GlobalItem();

    private Socket videoSocket;
    private Socket controlSocket;
    private Socket audioSocket;
}
