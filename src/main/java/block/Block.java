package block;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Block {

    // TODO: 22.03.2022 block must contain hash and value. Only block hash is sent 
    private final String content;
}
