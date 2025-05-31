package com.zariyo.concert.application.serializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.concert.api.exception.custom.CacheSerializationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@RequiredArgsConstructor
public class GzipRedisSerializer<T> implements RedisSerializer<T> {

    private final ObjectMapper objectMapper;
    private final TypeReference<T> typeRef;

    private static final byte[] GZIP_MAGIC_BYTES = new byte[]{
        (byte) (GZIPInputStream.GZIP_MAGIC & 0xFF),
        (byte) ((GZIPInputStream.GZIP_MAGIC >> 8) & 0xFF)
    };

    private static final int MIN_COMPRESS_SIZE = 2 * 1024; // 2KB 기준
    private static final int BUFFER_SIZE = 4 * 1024; // 4KB 버퍼

    @Override
    public byte[] serialize(T t) {
        if (t == null) {
            return null;
        }

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(t);
            return bytes.length > MIN_COMPRESS_SIZE ? compress(bytes) : bytes;
        } catch (Exception ex) {
            throw new CacheSerializationException("캐시 직렬화 중 오류가 발생했습니다.", ex);
        }
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        try {
            byte[] data = isGzipCompressed(bytes) ? decompress(bytes) : bytes;
            return objectMapper.readValue(data, typeRef);
        } catch (Exception ex) {
            throw new CacheSerializationException("캐시 역직렬화 중 오류가 발생했습니다.", ex);
        }
    }

    private byte[] compress(byte[] original) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(MIN_COMPRESS_SIZE);
             GZIPOutputStream gos = new GZIPOutputStream(bos, MIN_COMPRESS_SIZE)) {

            StreamUtils.copy(new ByteArrayInputStream(original), gos);
            gos.finish();
            return bos.toByteArray();

        } catch (IOException ex) {
            throw new CacheSerializationException("캐시 압축 중 오류가 발생했습니다.", ex);
        }
    }

    private byte[] decompress(byte[] encoded) {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(encoded));
             RawBufferByteArrayOutputStream out = new RawBufferByteArrayOutputStream(BUFFER_SIZE)) {

            StreamUtils.copy(gis, out);
            return out.getRawByteArray();

        } catch (IOException ex) {
            throw new CacheSerializationException("캐시 압축 해제 중 오류가 발생했습니다.", ex);
        }
    }

    private boolean isGzipCompressed(byte[] bytes) {
        return bytes.length > 2 &&
               bytes[0] == GZIP_MAGIC_BYTES[0] &&
               bytes[1] == GZIP_MAGIC_BYTES[1];
    }

    static class RawBufferByteArrayOutputStream extends ByteArrayOutputStream {

        public RawBufferByteArrayOutputStream(int size) {
            super(size);
        }

        public byte[] getRawByteArray() {
            return this.buf;
        }
    }
}
