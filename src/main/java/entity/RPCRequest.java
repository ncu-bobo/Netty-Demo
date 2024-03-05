package entity;

import lombok.*;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
@ToString
// 消息请求者
public class RPCRequest {
    private String interfaceName;
    private String methodName;
}
