package com.envisioncn.gssc.libra.security;

import lombok.Data;

/**
 * @author zhongshuangli
 * @date 2021-04-01
 */
@Data
public class User {
    private String username;
    private String password;
    private String roles[];
}
