package com.sacavix.ca;

import com.sacavix.ca.adapter.out.persistence.AccountEntity;
import com.sacavix.ca.adapter.out.persistence.SpringAccountRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Extra implements InitializingBean {

    private final SpringAccountRepository extra;

    public Extra(SpringAccountRepository extra) {
        this.extra = extra;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.extra.save(new AccountEntity(1L, BigDecimal.valueOf(20)));
        this.extra.save(new AccountEntity(2L, BigDecimal.valueOf(25)));
    }
}