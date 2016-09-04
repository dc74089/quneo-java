package com.explodingbacon.quneo;

public class APITester {
    public static void main(String... args) {
        MidiAPI api = new MidiAPI("QUNEO");
        MidiListener ml = new MidiListener();

        api.registerListener(ml, MidiAPI.NOTE_ON, 1, 36, 37, 38, 39);
    }

    private static class MidiListener implements MidiAPI.MidiAPIListener {
        @Override
        public void onMidiEvent(int eventType, int channel, byte note, byte data) {
            System.out.println(String.format("Got event %d on channel %d, note %d, data %d", eventType, channel, note, data));
        }
    }
}