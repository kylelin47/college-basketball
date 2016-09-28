package io.coachapps.collegebasketballcoach.util;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class SerializationUtil {
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
        } catch (IOException e) {
            Log.e("SerializationUtil", "Could not serialize object", e);
        }
        return bos.toByteArray();
    }
    public static Object deserialize(byte[] bytes) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
           return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("SerializationUtil", "Could not deserialize object", e);
        }
        return null;
    }
}
