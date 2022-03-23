package util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDataDto {

    private int code;
    private int len;
    private byte[] payload;
}
