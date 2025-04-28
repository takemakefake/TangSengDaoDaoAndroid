package com.chat.scan;

import com.chat.scan.entity.ScanResult;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * 2020-07-26 22:05
 * 扫描
 */
public interface ScanService {
    @GET
    Observable<ScanResult> getScanResult(@Url String url);
}
