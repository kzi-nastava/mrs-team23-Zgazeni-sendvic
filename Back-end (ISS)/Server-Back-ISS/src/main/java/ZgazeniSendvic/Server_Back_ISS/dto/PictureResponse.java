package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Picture;

import java.time.Instant;

public record PictureResponse(
        Long id,
        String url,
        String contentType,
        long size,
        Instant createdAt
) {
    public static PictureResponse from(Picture picture) {
        return new PictureResponse(
                picture.getId(),
                picture.getUrl(),
                picture.getContentType(),
                picture.getSize(),
                picture.getCreatedAt()
        );
    }
}
