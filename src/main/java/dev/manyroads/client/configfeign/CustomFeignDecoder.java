package dev.manyroads.client.configfeign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;

@Service
@Slf4j
public class CustomFeignDecoder implements Decoder {
    private final Gson gson = new Gson();

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        log.info("CustomFeignDecoder: decode started with status.code: {}",response.status());
        return gson.fromJson(response.body().asReader(), String.class);
    }
}
