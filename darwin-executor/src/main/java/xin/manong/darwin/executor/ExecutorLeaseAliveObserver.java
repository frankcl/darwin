package xin.manong.darwin.executor;

import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.darwin.common.Constants;
import xin.manong.darwin.common.model.Executor;
import xin.manong.darwin.service.iface.ExecutorService;

/**
 * 执行器租约活性检测
 *
 * @author frankcl
 * @date 2025-03-09 20:30:37
 */
public class ExecutorLeaseAliveObserver implements StreamObserver<LeaseKeepAliveResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorLeaseAliveObserver.class);

    private final String executorName;
    private final ExecutorService executorService;

    public ExecutorLeaseAliveObserver(String executorName, ExecutorService executorService) {
        this.executorName = executorName;
        this.executorService = executorService;
    }

    @Override
    public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
        logger.info("lease for global executor[{}] is alive for remaining TTL[{}]", executorName,
                leaseKeepAliveResponse.getTTL());
        if (executorService.get(executorName) == null) return;
        Executor updateExecutor = new Executor();
        updateExecutor.status = Constants.EXECUTOR_STATUS_RUNNING;
        updateExecutor.cause = "";
        if (!executorService.updateByName(executorName, updateExecutor)) {
            logger.warn("update running status failed for global executor[{}]", executorName);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("error occurred when keeping alive for global executor lease[{}]", executorName);
        Executor updateExecutor = new Executor();
        updateExecutor.status = Constants.EXECUTOR_STATUS_ERROR;
        updateExecutor.cause = throwable.getMessage();
        if (!executorService.updateByName(executorName, updateExecutor)) {
            logger.error("update error status failed for global executor[{}]", executorName);
        }
    }

    @Override
    public void onCompleted() {
        logger.info("global executor lease[{}] keep alive completed", executorName);
        Executor updateExecutor = new Executor();
        updateExecutor.status = Constants.EXECUTOR_STATUS_STOPPED;
        updateExecutor.cause = "";
        if (!executorService.updateByName(executorName, updateExecutor)) {
            logger.error("update stopped status failed for global executor[{}]", executorName);
        }
    }
}
