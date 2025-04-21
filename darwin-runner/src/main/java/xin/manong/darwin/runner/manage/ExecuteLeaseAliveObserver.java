package xin.manong.darwin.runner.manage;

import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.weapon.base.event.ErrorEvent;

/**
 * ExecuteRunner租约活性观察者
 *
 * @author frankcl
 * @date 2025-03-09 20:30:37
 */
public class ExecuteLeaseAliveObserver implements StreamObserver<LeaseKeepAliveResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteLeaseAliveObserver.class);

    private final ExecuteRunnerShell executeRunner;

    public ExecuteLeaseAliveObserver(ExecuteRunnerShell executeRunner) {
        this.executeRunner = executeRunner;
    }

    @Override
    public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
        logger.debug("Lease is alive for {}, TTL is {}", executeRunner.getKey(), leaseKeepAliveResponse.getTTL());
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("Keeping lease alive error for {}", executeRunner.getKey());
        executeRunner.onError(new ErrorEvent(throwable.getMessage(), throwable));
    }

    @Override
    public void onCompleted() {
        logger.info("Keep lease alive completed for {}", executeRunner.getKey());
        if (executeRunner.isRunning()) executeRunner.stop();
    }
}
