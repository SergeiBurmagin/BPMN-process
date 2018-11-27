package ru.starfish24.mascotte.bpm;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.starfish24.mascotte.repository.cancellationreason.CancellationReasonRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.repository.status.StatusRepository;
import ru.starfish24.mascotte.utils.ProcessVar;
import ru.starfish24.starfish24model.CancellationReason;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.Status;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class CancelOrderActivity implements JavaDelegate {

	@Autowired
	private ProductOrderRepository productOrderRepository;

	@Autowired
	private CancellationReasonRepository cancellationReasonRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Override
	@Transactional
	public void execute(DelegateExecution delegateExecution) throws Exception {
		Long orderId = Long.parseLong(delegateExecution.getVariable(ProcessVar.ORDER_ID).toString());
		Long accountId = Long.parseLong(delegateExecution.getVariable(ProcessVar.ACCOUNT_ID).toString());

		Object cancellationComment = delegateExecution.getVariable(ProcessVar.CANCELATION_COMMENT);
		Object cancellationCode = delegateExecution.getVariable(ProcessVar.CANCELATION_CODE);
		List<Status> statuses = statusRepository.findByAccount(accountId);
		Optional<Status> canceledStatusOpt = statuses.stream().filter(s -> Objects.equals(s.getCode(), "canceled")).findFirst();
		if (!canceledStatusOpt.isPresent()) {
			log.error("status [code = canceled}, orderId = {}] not found", orderId);
		}
		ProductOrder productOrder = productOrderRepository.findOne(orderId);
		productOrder.setCanceled(true);
		canceledStatusOpt.ifPresent(productOrder::setCurrentStatus);
		productOrder.setTimeCanceled(new Date());
		productOrder.setCancelationComment((String) cancellationComment);
		if (cancellationCode != null && productOrder.getCancellationReason().getCode() ==  null) {
			CancellationReason cancellationReason = cancellationReasonRepository.findByCodeAndShopId(cancellationCode.toString(), productOrder.getShop().getId());
			productOrder.setCancellationReason(cancellationReason);
		}

		//processEventSender.sendMessage(EventCode.ORDER_CANCELED.name(), new HashMap<>());
		log.info("product order canceled [orderId = {}, cancelationCode = {}]", orderId, cancellationCode);
	}

}