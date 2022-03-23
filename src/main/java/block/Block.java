package block;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Block {

    private String hash;
    private String transaction;
}
