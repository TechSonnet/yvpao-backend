package com.sonnet.exception;

import com.sonnet.common.ErrorCode;

/**
 * 业务自定义异常
 */
public class BusinessException extends RuntimeException{

    // 当不需要 set 的时候，可选择使用 final
    private final int code;
    private final String description;

    // 相当于多封装了一些参数
    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    // 接下来这几个函数要十分注意，我们在实际编码时，抛出的就是这几个函数
    // 使用了多态
    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    // 这些都并非固定的写法，可以根据自己的需要封装一些构造函数，方便自己的使用
    // 做人要灵活，写代码也是
    public BusinessException(ErrorCode errorCode, String description){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
