package com.drb.DrbMVP.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.MalformedURLException;
import java.net.URL;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;

    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client, s3Presigner, "test-bucket", "eu-west-1");
    }

    @Test
    void upload_validJpegFile_returnsS3Key() {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "photo.jpg", "image/jpeg", new byte[100]
        );
        Mockito.when(s3Client.putObject(
                Mockito.any(PutObjectRequest.class),
                Mockito.any(RequestBody.class)
        )).thenReturn(null);

        String key = s3Service.upload(file, "reviews/1");

        Assertions.assertNotNull(key);
        Assertions.assertTrue(key.startsWith("reviews/1/"));
        Assertions.assertTrue(key.endsWith(".jpg"));
    }

    @Test
    void upload_validPngFile_returnsS3Key() {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "image.png", "image/png", new byte[200]
        );
        Mockito.when(s3Client.putObject(
                Mockito.any(PutObjectRequest.class),
                Mockito.any(RequestBody.class)
        )).thenReturn(null);

        String key = s3Service.upload(file, "reviews/2");

        Assertions.assertTrue(key.endsWith(".png"));
    }

    @Test
    void upload_emptyFile_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "empty.jpg", "image/jpeg", new byte[0]
        );

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> s3Service.upload(file, "reviews/1"));

        Mockito.verify(s3Client, Mockito.never())
                .putObject(Mockito.any(PutObjectRequest.class), Mockito.any(RequestBody.class));
    }

    @Test
    void upload_fileTooLarge_throwsIllegalArgumentException() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile(
                "photo", "large.jpg", "image/jpeg", largeContent
        );

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> s3Service.upload(file, "reviews/1"));
    }

    @Test
    void upload_unsupportedContentType_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "doc.pdf", "application/pdf", new byte[100]
        );

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> s3Service.upload(file, "reviews/1"));
    }

    @Test
    void delete_callsS3ClientWithCorrectKey() {
        s3Service.delete("reviews/1/photo.jpg");

        Mockito.verify(s3Client).deleteObject(Mockito.argThat((DeleteObjectRequest req) ->
                req.bucket().equals("test-bucket") &&
                        req.key().equals("reviews/1/photo.jpg")
        ));
    }

    @Test
    void buildUrl_returnsPresignedUrl() throws MalformedURLException {
        PresignedGetObjectRequest presigned = Mockito.mock(PresignedGetObjectRequest.class);
        Mockito.when(presigned.url()).thenReturn(new URL("https://s3.amazonaws.com/test-bucket/key?sig=abc"));
        Mockito.when(s3Presigner.presignGetObject(Mockito.any(GetObjectPresignRequest.class)))
                .thenReturn(presigned);

        String url = s3Service.buildUrl("reviews/1/photo.jpg");

        Assertions.assertNotNull(url);
        Assertions.assertTrue(url.startsWith("https://"));
    }
}
