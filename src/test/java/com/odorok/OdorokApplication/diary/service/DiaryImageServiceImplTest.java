package com.odorok.OdorokApplication.diary.service;


import com.odorok.OdorokApplication.diary.repository.DiaryImageRepository;
import com.odorok.OdorokApplication.domain.DiaryImage;
import com.odorok.OdorokApplication.s3.service.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DiaryImageServiceImplTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private DiaryImageRepository diaryImageRepository;

    @InjectMocks
    private DiaryImageServiceImpl diaryImageService;


    @Test
    public void 일지_이미지_생성_성공() {
        long userId = 1L;
        List<MultipartFile> images = List.of(mock(MultipartFile.class), mock(MultipartFile.class));
        String DOMAIN = "diary";

        List<String> mockUploadManyImage = List.of(
                "https://s3.amazonaws.com/diary/1/image1.jpg",
                "https://s3.amazonaws.com/diary/1/image2.jpg"
        );

        when(s3Service.uploadMany(eq(DOMAIN), eq(String.valueOf(userId)), eq(images))).thenReturn(mockUploadManyImage);

        // when
        List<String> result = diaryImageService.insertDiaryImage(images, userId);

        // then
        assertNotNull(result);
        assertEquals(mockUploadManyImage, result);
        assertEquals(2, result.size());
        verify(s3Service, times(1)).uploadMany(DOMAIN, String.valueOf(userId), images);
    }

    @Test
    public void 일지_이미지_url_DB_생성_성공() {
        List<String> imgUrls = List.of(
                "https://s3.amazonaws.com/diary/1/image1.jpg",
                "https://s3.amazonaws.com/diary/1/image2.jpg"
        );
        Long diaryId = 1L;
        // when
        diaryImageService.insertDiaryImageUrl(imgUrls, diaryId);

        // then
        // DiaryImage 타입의 인자를 캡처할 수 있는 캡처기를 생성
        ArgumentCaptor<DiaryImage> captor = ArgumentCaptor.forClass(DiaryImage.class);

        // save()가 2번 호출됐는지 검증, 각 호출의 인자를 캡처한다.
        verify(diaryImageRepository, times(2)).save(captor.capture());

        // 캡처된 모든 인자를 리스트로 꺼낸다.
        List<DiaryImage> savedImages = captor.getAllValues();

        // 첫 번째 save()에 전달된 DiaryImage의 imageUrl과 일치하는지 검증
        assertEquals("https://s3.amazonaws.com/diary/1/image1.jpg", savedImages.get(0).getImgUrl());
        // 두 번째 save()에 전달된 DiaryImage의 imageUrl과 일치하는지 검증
        assertEquals("https://s3.amazonaws.com/diary/1/image2.jpg", savedImages.get(1).getImgUrl());

//        verify(diaryImageRepository, times(3)).save(any(DiaryImage.class)); // 이건 그냥 호출 횟수만 검증하는 것
    }
}
