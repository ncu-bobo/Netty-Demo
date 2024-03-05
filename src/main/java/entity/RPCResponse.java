package entity;

import lombok.*;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
@ToString
public class RPCResponse {
    private String message;
}
