package com.explodingbacon.quneo;

import javax.sound.midi.*;

import static javax.sound.midi.ShortMessage.*;

public class MidiAPI {
    private Transmitter t = new Transmitter();
    private Receiver mReceiver = new Receiver();

    public MidiAPI(String deviceName) {
        MidiDevice device = null;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        Boolean foundDevice = false;
        for(MidiDevice.Info info : infos) {
            if(info.getName().equals(deviceName)) {
                foundDevice = true;
                System.out.println("Found a matching device");

                try {
                    device = MidiSystem.getMidiDevice(info);
                    device.getTransmitter().setReceiver(mReceiver);

                    System.out.println("Found a readable device: " + info);

                    device.open();
                } catch (MidiUnavailableException ignored) {
                    try {
                        device = MidiSystem.getMidiDevice(info);
                        t.setReceiver(device.getReceiver());

                        System.out.println("Found a writable device: " + info);

                        device.open();
                    } catch (MidiUnavailableException e) {
                        throw new RuntimeException("There was an error trying to open the MIDI device.", e);
                    }
                }
            }
        }

        if(!foundDevice) throw new MidiDeviceNotFoundException("We were unable to find a device that you specified.");
    }

    public interface MidiAPIListener {
        void onMidiEvent(EventType eventType, int channel, byte note, byte data);
    }

    public void registerListener(EventType type, int channel, byte note) {
        //TODO: This.
    }

    private class Transmitter implements javax.sound.midi.Transmitter {
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

        public void sendCC(byte note, byte data) {
            sendCC(1, note, data);
        }

        public void sendCC(int channel, byte note, byte data) {
            try {
                MidiMessage m = new ShortMessage(CONTROL_CHANGE, channel, note, data);
                r.send(m, System.currentTimeMillis());
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }

        public void sendNote(Boolean on, byte note, byte channel) {
            sendNote(on, 1, note, channel);
        }

        public void sendNote(Boolean on, int channel, byte note, byte velocity) {
            try {
                MidiMessage m = new ShortMessage(on ? NOTE_ON : NOTE_OFF, channel, note, velocity);
                r.send(m, System.currentTimeMillis());
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    private class Receiver implements javax.sound.midi.Receiver {

        @Override
        public void send(MidiMessage message, long timeStamp) {
            //TODO: Handle incoming messages
        }

        @Override
        public void close() {

        }
    }

    private class MidiDeviceNotFoundException extends RuntimeException {
        MidiDeviceNotFoundException(String cause) {
            super(cause);
        }
    }

    public interface EventType {
        int CONTROL_CHANGE = -80;
        int NOTE_ON = -112;
        int NOTE_OFF = -128;
    }
}
