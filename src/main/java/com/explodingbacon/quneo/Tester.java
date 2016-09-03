package com.explodingbacon.quneo;

import javax.sound.midi.*;
import java.io.IOException;

import static javax.sound.midi.ShortMessage.*;

public class Tester {
    private static Transmitter t = new Transmitter();
    private static Receiver mReceiver = new Receiver();

    /**
     * Note on: -112
     * Note off: -128
     * Control Change: -80
     **/

    public static void main(String ... args) throws IOException {
        MidiDevice device = null;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        for(MidiDevice.Info info : infos) {
            if(info.getName().equals("QUNEO")) {
                System.out.println("Found a QUNEO");
                try {
                    device = MidiSystem.getMidiDevice(info);
                    device.getTransmitter().setReceiver(mReceiver);

                    System.out.println("Found a QuNeo with Transmitters!: " + info.getDescription());

                    device.open();
                } catch (MidiUnavailableException ignored) {
                    try {
                        device = MidiSystem.getMidiDevice(info);
                        t.setReceiver(device.getReceiver());
                        device.open();
                    } catch (MidiUnavailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        while(true);
    }

    public static class Receiver implements javax.sound.midi.Receiver {

        @Override
        public void send(MidiMessage message, long timeStamp) {
            String msg = "";

            for(byte b : message.getMessage()) {
                msg += (b + ", ");
            }
            msg = msg.substring(0, msg.length()-2);

            System.out.println("Message Received: " + msg);

            byte[] m = message.getMessage();

            if(m[0] == -112) { //Note On
                if (m[1] == 19) {
                    t.allOff();
                } else if(6 <= m[1] && m[1] <= 9) {
                    t.sweepUpCC(1, 2, 3, 4);
                } else if(0 <= m[1] && m[1] <= 3) {
                    t.sweepUpCC(8, 9, 10, 11);
                } else if(m[1] == 4 || m[1] == 5) {
                    t.sweepUpCC(6, 7);
                } else if(m[1] == 10) {
                    t.sweepUpCC(5);
                } else if(m[1] >= 36 && m[1] <= 51) {
                    //t.sweepUpNote(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
                    //        25, 26, 27, 28, 29, 30, 31);
                    t.sweepUpNote(0, 3, 4, 7, 9, 10, 13, 14, 16, 19, 20, 23, 25, 26, 29, 30);
                } else if((11 <= m[1] && m[1] <= 18) || (20 <= m[1] && m[1] <= 26)) {
                    t.sweepUpNote(33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49);
                }

            } else if(m[0] == -128){ //Note Off

            } else { //Control Change... Probably

            }
        }

        @Override
        public void close() {

        }
    }

    public static class Transmitter implements javax.sound.midi.Transmitter {
        private javax.sound.midi.Receiver r;

        @Override
        public void setReceiver(javax.sound.midi.Receiver receiver) {
            this.r = receiver;
        }

        @Override
        public javax.sound.midi.Receiver getReceiver() {
            return r;
        }

        @Override
        public void close() {

        }

        public void allOff() {
            for(byte i = 1; i < 12; i++) {
                sendCC(i, (byte) 0);
            }
            for(byte i = 0; i < 50; i++) {
                sendNote(false, i, (byte) 0);
            }
        }

        public void sweepUpCC(int ... channel) {
            for(byte i = 0; i < 127; i++) {
                for(int c : channel)
                    t.sendCC((byte) c, i);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sweepUpNote(int ... channel) {
            for(byte i = 0; i < 127; i++) {
                for(int c : channel)
                    t.sendNote(true, (byte) c, i);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendCC(byte note, byte data) {
            try {
                MidiMessage m = new ShortMessage(CONTROL_CHANGE, note, data);

                r.send(m, System.currentTimeMillis());
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }

        public void sendNote(Boolean on, byte note, byte velocity) {
            try {
                MidiMessage m = new ShortMessage(on ? NOTE_ON : NOTE_OFF, note, velocity);

                r.send(m, System.currentTimeMillis());
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }
}
