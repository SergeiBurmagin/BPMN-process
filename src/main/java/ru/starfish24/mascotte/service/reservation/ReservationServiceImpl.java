package ru.starfish24.mascotte.service.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.client.api.InternalReservationControllerApi;
import io.swagger.client.model.InternalReservationDto;
import io.swagger.client.model.InternalReservationRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.starfish24.mascotte.bpm.exportreservation.dto.ShipToCustomerResultDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.TransitionDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.WarehouseDto;
import ru.starfish24.mascotte.bpm.exportreservation.dto.WarehousesMapping;
import ru.starfish24.mascotte.dto.IntegrationServiceResponse;
import ru.starfish24.mascotte.dto.reservation.CancelReservationRequest;
import ru.starfish24.mascotte.dto.reservation.Product;
import ru.starfish24.mascotte.dto.reservation.ProductRequest;
import ru.starfish24.mascotte.dto.reservation.ReservationRequest;
import ru.starfish24.mascotte.dto.transition.CreateTransitionDto;
import ru.starfish24.mascotte.http.HttpClient;
import ru.starfish24.mascotte.repository.integrationmicroservice.IntegrationMicroserviceRepository;
import ru.starfish24.mascotte.repository.product.ProductRepository;
import ru.starfish24.mascotte.repository.productorder.ProductOrderRepository;
import ru.starfish24.mascotte.repository.warehouse.WarehouseRepository;
import ru.starfish24.starfish24model.IntegrationMicroservice;
import ru.starfish24.starfish24model.ProductOrder;
import ru.starfish24.starfish24model.attributes._common.AbstractAttributeValue;
import ru.starfish24.starfish24model.attributes.order.OrderAttributeValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String RESERVATION_REQUEST_TYPE = "reservation";
    public static final String CANCEL_RESERVATION_REQUEST_TYPE = "cancelReservation";
    public static final String CANCEL_TRANSFER_REQUEST_TYPE = "cancelTransfer";

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private IntegrationMicroserviceRepository integrationMicroserviceRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private HttpClient httpClient;

    @Autowired
    InternalReservationControllerApi internalReservationControllerApi;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ReservationService reservationService;

    @Override
    @Transactional
    public IntegrationServiceResponse createReservation(Long orderId, List<Product> products, String warehouseExternalId, String transitWarehouseExternalId, String targetWarehouseExternalId) {
        ProductOrder productOrder = productOrderRepository.findOne(orderId);
        Long accountId = productOrder.getShop().getAccount().getId();
        IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(accountId, RESERVATION_REQUEST_TYPE);
        if (integrationMicroservice == null) {
            log.error("integration microservice [type = 'reservation', accountId = {}] NOT FOUND", accountId);
            throw new RuntimeException(String.format("No service regisered for [accountid=%s] and [type%s]", accountId, RESERVATION_REQUEST_TYPE));
        }
        ReservationRequest reservationRequest = new ReservationRequest();
        Optional<OrderAttributeValue> orderGuid = productOrder.getAttributeValues().stream().filter(v -> Objects.equals(v.getAttribute().getCode(), "order_guid")).findFirst();
        reservationRequest.setOrderGUID(orderGuid.map(AbstractAttributeValue::getValue).orElse(null));
        reservationRequest.setOrderId(productOrder.getExternalId());
        reservationRequest.setTargetWarehouseId(targetWarehouseExternalId);
        reservationRequest.setWarehouseId(warehouseExternalId);
        reservationRequest.setTransitWarehouseId(transitWarehouseExternalId);
        reservationRequest.setSkuList(products.stream().map(this::createProductRequest).collect(Collectors.toList()));
        IntegrationServiceResponse integrationServiceResponse = null;
        try {
            String json = httpClient.postRequest(integrationMicroservice.getUri(), reservationRequest);
            integrationServiceResponse = MAPPER.readValue(json, IntegrationServiceResponse.class);
        } catch (Exception ex) {
            integrationServiceResponse = new IntegrationServiceResponse();
            integrationServiceResponse.setMessage(ex.toString());
            integrationServiceResponse.setSuccess(false);
            log.error("Request to integration service failed", ex);
        }
        return integrationServiceResponse;

    }


    @Transactional
    @Override
    public IntegrationServiceResponse createTransfer(@NonNull Long orderId, List<Product> products, String externalToId, String externalFromId) {
        ProductOrder productOrder = productOrderRepository.getOne(orderId);
        Long accountId = productOrder.getShop().getAccount().getId();
        IntegrationMicroservice integrationMicroservice = integrationMicroserviceRepository.findByAccountIdAndType(accountId, "transfer");
        if (integrationMicroservice == null) {
            log.error("integration microservice [type = 'reservation', accountId = {}] NOT FOUND", accountId);
            return null;
        }
        CreateTransitionDto createTransitionDto = new CreateTransitionDto();
        createTransitionDto.setOrderId(productOrder.getExternalId());
        createTransitionDto.setTargetWarehouseId(externalToId);
        createTransitionDto.setWarehouseId(externalFromId);
        createTransitionDto.setSkuList(products.stream().map(this::createProductRequest).collect(Collectors.toList()));
        String response = null;
        IntegrationServiceResponse result = null;
        try {
            response = httpClient.postRequest(integrationMicroservice.getUri(), createTransitionDto);
            result = MAPPER.readValue(response, IntegrationServiceResponse.class);
        } catch (IOException ex) {
            result = new IntegrationServiceResponse();
            result.setMessage(ex.toString());
            result.setSuccess(false);
            log.error("Request to integration service failed", ex);
        }
        return result;
    }


    private ProductRequest createProductRequest(Product product) {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setExternalId(productRepository.findOne(product.getProductId()).getExternalId());
        if (product.getQuantity() != null) {
            productRequest.setQuantity(product.getQuantity());
        }
        return productRequest;
    }

    @Override
    @Transactional
    public List<InternalReservationDto> createInternalReservations(List<ShipToCustomerResultDto> reservationInfoes) {
        return reservationInfoes.stream().filter(r -> r.getReserve() != null && r.getReserve().getExternalId() != null)
                .map(this::createInternalReservation).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public InternalReservationDto confirmInternalTransfer(InternalReservationDto internalReservationDto) {
        return internalReservationControllerApi.confirmTransferUsingPOST(internalReservationDto.getId());
    }

    @Override
    @Transactional
    public InternalReservationDto cancelInternalTransfer(InternalReservationDto internalReservationDto) {
        return internalReservationControllerApi.cancelTransferUsingPOST(internalReservationDto.getId());
    }

    @Override
    @Transactional
    public InternalReservationDto confirmInternalReservation(InternalReservationDto reservationDto, String confirmID) {
        return internalReservationControllerApi.confirmUsingPOST(confirmID, reservationDto.getId());
    }

    @Override
    @Transactional
    public InternalReservationDto internalReservationFailed(InternalReservationDto internalReservationDto) {
        return internalReservationControllerApi.failedUsingPOST(internalReservationDto.getId());
    }

    @Override
    public InternalReservationDto internalReservationCanceled(InternalReservationDto internalReservationDto) {
        return internalReservationControllerApi.canceledUsingPOST(internalReservationDto.getId());
    }

    private InternalReservationDto createInternalReservation(ShipToCustomerResultDto shipToCustomerResultDto) {

        double quantity = shipToCustomerResultDto.getProductOrderItem().getQuantity();

        WarehouseDto target = shipToCustomerResultDto.getTarget();
        WarehouseDto reserve = shipToCustomerResultDto.getReserve();
        WarehouseDto transit = shipToCustomerResultDto.getTransit();
        Long orderItemId = shipToCustomerResultDto.getProductOrderItem().getId();
        TransitionDto transition = shipToCustomerResultDto.getTransition();
        return createInternalReservationDto(InternalReservationRequest.StatusEnum.DO_RESERVE, quantity, target, reserve, transit, orderItemId, transition);
    }

    @Override
    public InternalReservationDto createInternalReservationDto(InternalReservationRequest.StatusEnum status, double quantity, WarehouseDto target, WarehouseDto reserve, WarehouseDto transit, Long orderItemId, TransitionDto transition) {
        Long targetWarehouseId = getWarehouseId(target);
        Long reserveWarehouseId = getWarehouseId(reserve);
        Long transitWarehouseId = getWarehouseId(transit);
        Long fromWarehouseId = transition != null ? getWarehouseId(transition.getFrom()) : null;
        Long toWarehouseId = transition != null ? getWarehouseId(transition.getTo()) : null;

        return createInternalReservationDto(status, quantity, orderItemId, targetWarehouseId, reserveWarehouseId, transitWarehouseId, fromWarehouseId, toWarehouseId);
    }

    @Override
    public InternalReservationDto createInternalReservationDto(InternalReservationRequest.StatusEnum status, double quantity, Long orderItemId, Long targetWarehouseId, Long reserveWarehouseId, Long transitWarehouseId, Long fromWarehouseId, Long toWarehouseId) {
        InternalReservationRequest request = new InternalReservationRequest();
        request.setQuantity(quantity);
        request.setStatus(status);
        request.setTargetWarehouseId(targetWarehouseId);
        request.setReserveWarehouseId(reserveWarehouseId);
        request.setTransitWarehouseId(transitWarehouseId);
        request.setOrderItemId(orderItemId);

        if (fromWarehouseId != null && toWarehouseId != null)
            request.setTransfer(true);
        else
            request.setTransfer(false);


        request.setTransferFromId(fromWarehouseId);
        request.setTransferToId(toWarehouseId);
        return internalReservationControllerApi.createResponseUsingPUT(request);
    }

    @Override
    public IntegrationServiceResponse cancelTransfer(InternalReservationDto internalReservationDto) throws IOException {
        //internalReservationDto.getOrderItemId();
        IntegrationServiceResponse res = new IntegrationServiceResponse();
        res.setSuccess(true);
        res.setMessage("Cancel transfer method not implemented");
        return res;
        /*String result = httpClient.postRequest(internalReservationDto.getAccountId(),CANCEL_TRANSFER_REQUEST_TYPE, null);
        return  MAPPER.readValue(result, IntegrationServiceResponse.class);*/
    }

    @Override
    public IntegrationServiceResponse cancelReserve(InternalReservationDto internalReservationDto) throws IOException {
        CancelReservationRequest cancelReservationRequest = new CancelReservationRequest();
        cancelReservationRequest.setOrderId(internalReservationDto.getExternalOrderId());
        List<ProductRequest> products = new ArrayList<>(1);
        ProductRequest r = new ProductRequest();
        r.setExternalId(internalReservationDto.getProductExternalId());
        r.setQuantity(internalReservationDto.getQuantity().intValue());
        products.add(r);
        cancelReservationRequest.setSkuList(products);
        String result = httpClient.postRequest(internalReservationDto.getAccountId(), CANCEL_RESERVATION_REQUEST_TYPE, cancelReservationRequest);
        return MAPPER.readValue(result, IntegrationServiceResponse.class);
    }

    @Override
    public void doReservationByInternalReservationDtoList(Long orderId, List<InternalReservationDto> internalReservationDtos)
            throws IOException {
        Assert.notNull(internalReservationDtos, "doReservationByInternalReservationDtoList() source list is null");
        // todo доработать метод
        //Выполнение операции резервирования
        internalReservationDtos.stream().filter(r ->
                !r.getTransferStatus().equals(InternalReservationDto.TransferStatusEnum.CANCELED)
                        && r.getReserveWarehouse() != null)
                .collect(Collectors.groupingBy(s -> new WarehousesMapping(s.getReserveWarehouse(), s.getTargetWarehouse(), s.getTransitWarehouse())))
                .forEach((key, value) -> {
                    String warehouseExternalId = key.getReserve() == null ? null : key.getReserve().getExternalId();
                    String transitWarehouseExternalId = key.getTransit() == null ? null : key.getTransit().getExternalId();
                    String targetWarehouseExnternalId = key.getTarget() == null ? null : key.getTarget().getExternalId();
                    IntegrationServiceResponse response = reservationService.createReservation(orderId, getProducts(value), warehouseExternalId, transitWarehouseExternalId, targetWarehouseExnternalId);
                    if (response.getSuccess()) {
                        String message = response.getMessage().isEmpty() ? "Reservation is created" : response.getMessage();
                        value.forEach(r -> {
                            reservationService.confirmInternalReservation(r, message);
                        });
                    } else {
                        value.forEach(r -> {
                            reservationService.internalReservationFailed(r);
                        });
                    }

                });
    }

    @Override
    public void doTransferByInternalReservationDtoList(Long orderId, List<InternalReservationDto> internalReservationDtos)
            throws IOException {
        Assert.notNull(internalReservationDtos, "doTransferByInternalReservationDtoList() source list is null");
        // todo доработать метод
        //Выполнение операции перемещения.
        internalReservationDtos.stream()
                .filter(r -> InternalReservationDto.TransferStatusEnum.TRANSFER.equals(r.getTransferStatus()))
                .collect(Collectors.groupingBy(InternalReservationDto::getTransferWarehouseFrom))
                .entrySet().forEach(fromGroup -> fromGroup.getValue().stream().collect(Collectors.groupingBy(toGroup -> toGroup.getTransferWarehouseTo()))
                .entrySet().forEach(toGroup -> {
                            List<Product> products = getProducts(toGroup.getValue());
                            IntegrationServiceResponse response = reservationService.createTransfer(orderId, products, toGroup.getKey().getExternalId(), fromGroup.getKey().getExternalId());
                            if (!response.getSuccess()) {
                                toGroup.getValue().forEach(res -> {
                                    internalReservationDtos.remove(res);
                                    internalReservationDtos.add(reservationService.cancelInternalTransfer(res));
                                    log.error("Failed to transfer order =[{}] items = {}", orderId, products.stream().map(p -> p.toString()).collect(Collectors.joining(",")));
                                });
                            } else {
                                toGroup.getValue().forEach(res -> {
                                    internalReservationDtos.remove(res);
                                    internalReservationDtos.add(reservationService.confirmInternalTransfer(res));
                                });
                            }

                        }
                )
        );
    }

    @Override
    public List<Product> getProducts(List<InternalReservationDto> toGroup) {
        return toGroup.stream()
                .map(x -> new Product(x.getProductId(), x.getQuantity()))
                .collect(Collectors.toList());
    }

    @Override
    public Long getWarehouseId(WarehouseDto dto) {
        if (dto == null || dto.getExternalId() == null) return null;
        return warehouseRepository.findOneByExternalId(dto.getExternalId()).getId();
    }
}
