package com.sacavix.ca.service;

import com.sacavix.ca.application.port.in.SendMoneyCommand;
import com.sacavix.ca.application.port.in.SendMoneyPort;
import com.sacavix.ca.application.port.out.LoadAccountPort;
import com.sacavix.ca.application.port.out.UpdateAccountPort;
import com.sacavix.ca.common.UseCase;
import com.sacavix.ca.domain.Account;

import javax.transaction.Transactional;

@UseCase
public class SendMoneyService implements SendMoneyPort {

    private final LoadAccountPort loadAccountPort;
    private final UpdateAccountPort updateAccountPort;

    public SendMoneyService(LoadAccountPort loadAccountPort, UpdateAccountPort updateAccountPort) {
        this.loadAccountPort = loadAccountPort;
        this.updateAccountPort = updateAccountPort;
    }

    @Transactional
    @Override
    public boolean send(SendMoneyCommand command) {

        Account source = loadAccountPort.load(command.getSourceId());
        Account target = loadAccountPort.load(command.getTargetId());

        if(!source.isBalanceGreaterThan(command.getAmount())) {
            throw new RuntimeException("Source account not have enough balance amount ... ");
        }

        target.plus(command.getAmount());
        source.subtract(command.getAmount());

        updateAccount(source);
        updateAccount(target);

        return true;
    }

    private void updateAccount(Account account) {
        try {
            for (java.lang.reflect.Method method : updateAccountPort.getClass().getMethods()) {
                if (method.getName().equals("update")
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isAssignableFrom(account.getClass())) {
                    method.invoke(updateAccountPort, account);
                    return;
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not update account", e);
        }
        throw new IllegalStateException("No compatible account update method found");
    }
}