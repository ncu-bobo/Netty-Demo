package serialize;

import com.esotericsoftware.kryo.Kryo;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import entity.RPCRequest;
import entity.RPCResponse;
import exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

//序列化对象
public class KryoSerializer implements Serializer {
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RPCRequest.class);
        kryo.register(RPCResponse.class);
        return kryo;
    });

    //序列化
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            //Object->byte: 将对象序列化为byte数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        }catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }
    }

    //反序列化
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte->Object:从byte数组中反序列化出对对象
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(o);
        } catch (Exception e) {
            throw new SerializeException("反序列化失败");
        }
    }

}
