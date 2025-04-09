package com.ffm.rtc;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.util.concurrent.TimeUnit;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class Main {
    public static void main(String[] args) {
        System.setProperty("jextract.trace.downcalls", "true");
        try (var arena = Arena.ofShared()) {
            MemorySegment iceConfig = IceConfig.allocate(arena);
            IceConfig.noStun(iceConfig, 1);
            IceConfig.stunServer(iceConfig, arena.allocateUtf8String("stun.l.google.com"));
            IceConfig.stunPort(iceConfig, 19302);
            IceConfig.udpMux(iceConfig, 1);
            IceConfig.webSocketServer(iceConfig, arena.allocateUtf8String("127.0.0.1"));
            IceConfig.webSocketPort(iceConfig, 8000);

            MemorySegment dataChannelConfig = DataChannelConfig.allocate(arena);
            DataChannelConfig.onOpen(dataChannelConfig, DataChannelConfig.onOpen.allocate(() -> {
                System.out.println("datachannel open");
            }, arena));
            DataChannelConfig.onTextMessage(dataChannelConfig, DataChannelConfig.onTextMessage.allocate((MemorySegment msg) -> {
                System.out.println("datachannel textmessage");
                System.out.println(msg.getUtf8String(0));
            }, arena));
            DataChannelConfig.onClosed(dataChannelConfig, DataChannelConfig.onClosed.allocate(() -> {
                System.out.println("datachannel closed");
            }, arena));
            DataChannelConfig.onBinaryMessage(dataChannelConfig, DataChannelConfig.onBinaryMessage.allocate((MemorySegment msg, long size) -> {
                System.out.println("datachannel binarymessage");
            }, arena));
            RtcHelper.createRtcContext(arena.allocateUtf8String("abcd"), 4, iceConfig, dataChannelConfig);
            /*Linker linker = Linker.nativeLinker();
            MethodHandle createRtcContext = linker.downcallHandle(
                    RtcHelper.SYMBOL_LOOKUP.find("createRtcContext").orElseThrow(),
                    FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS, ADDRESS)
            );
            try {
                createRtcContext.invokeExact(arena.allocateUtf8String("abcd"), 4, iceConfig, dataChannelConfig);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }*/

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}