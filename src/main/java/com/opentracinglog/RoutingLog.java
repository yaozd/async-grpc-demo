package com.opentracinglog;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.UUID;

import static com.StringConstant.EMPTY_STR;
import static com.StringConstant.HYPHEN;

/**
 * @Author: yaozh
 * @Description:
 */
//@Getter
//@Setter
@Slf4j
@Data
public class RoutingLog {
    private static final String DATE_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final int UNKNOWN_STATUS = -1;
    public static final int SUCCESS_STATUS = 200;
    private final long requestStartTime;
    private final String requestTimeStr;
    private final String traceId;
    private String role;
    private String host;
    private String uri;
    private String grpcService;
    private long totalCost;
    private String requestType;
    private String targetIp = HYPHEN;
    private String entryIp = HYPHEN;
    /**
     * 后端响应状态
     */
    private int targetResponseCode = UNKNOWN_STATUS;
    /**
     * 内部响应状态
     */
    private int innerResponseCode = UNKNOWN_STATUS;
    private String interruptMessage = EMPTY_STR;

    public RoutingLog() {
        this.requestStartTime = System.currentTimeMillis();
        this.requestTimeStr = DateFormatUtils.format(requestStartTime, DATE_TIME_FORMATTER);
        this.traceId = StringUtils.remove(UUID.randomUUID().toString(), '-');
    }

    public void finish() {
        this.totalCost = System.currentTimeMillis() - this.requestStartTime;
        log.info("[FINISH]:{}", toString());
    }
}
