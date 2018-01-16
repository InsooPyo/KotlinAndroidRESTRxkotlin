package com.pyoinsoo.rest.kotlin.rxkotlin

import android.os.Bundle
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.pyoinsoo.rest.kotlin.rxkotlin.jsondata.CurrentWeather
import com.pyoinsoo.rest.kotlin.rxkotlin.jsondata.Minutely
import com.pyoinsoo.rest.kotlin.rxkotlin.restful.RxkotlinRetrofitOkHttp3WarmUp
import com.pyoinsoo.rest.kotlin.rxkotlin.restful.WeatherRESTInterface
import com.pyoinsoo.rest.kotlin.rxkotlin.common.WeatherUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
         * 현재 날짜정보를 TextView에 출력한다
         */
        tvTodayDate.text = WeatherUtil.currentDate()
        tvTodayAmPm.text = WeatherUtil.amPm()
        tvTodayHhMm.text = WeatherUtil.minuteSecond()

    }

    /*
     * 요청을 보낼 질의문자열의 값을 정의
     */
    private val latitude = "37.572978"
    private val logitude = "126.989061"
    private val apiVersion = "1"

    private var rxDisposable = CompositeDisposable()

    override fun onResume() {
        super.onResume()
        /*
         * 요청을 보낼 Retrofit interface를 생성해 가져온다
         */
        val restClient: WeatherRESTInterface =
              RxkotlinRetrofitOkHttp3WarmUp.
                      createSKWeatherRESTService(WeatherRESTInterface::class.java)

        /*
         * 현재 날씨를 백그라운드 쓰레드를 이용해
         * json을 가져오고 데이터객체로 변환한다
         */
        var observer: Observable<CurrentWeather> =
                restClient.requestSKCurrentWeather(latitude,logitude,apiVersion)

        var dispose = observer.subscribeOn(Schedulers.io())
                              .observeOn(AndroidSchedulers.mainThread())
                              .subscribe({
                                  setUICurrentWeather(it)  //it은 CurrentWeather data객체
                                }, { t ->
                                    errorMessage(t.toString())
                                })
        /*
         * 해제할 RX자원을 추가한다
         */
        rxDisposable.add(dispose)

        /*
         * 미세먼지 정보를 가져오고 자원해제를 위해 추가한다
         */
        observer = restClient.requestSKFineDust(latitude,logitude,apiVersion)
        dispose = observer.subscribeOn(Schedulers.io())
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe({
                              setUIDust(it) //데이터객체를 UI업데이트를 위해 넘긴다
                          }, { t ->
                              errorMessage(t.toString())
                          })
        rxDisposable.add(dispose)

        /*
         * 자외선 정보를 가져오고 자원해제를 위해 추가한다
         */
        observer = restClient.requestSKUvRays(latitude,logitude,apiVersion)
        dispose = observer.subscribeOn(Schedulers.io())
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe({
                               setUIUvindex(it) //데이터객체를 UI업데이트를 위해 넘긴다
                           }, { t ->
                               errorMessage(t.toString())
                           })
        rxDisposable.add(dispose)
    }
    /*
     * Activity Life Cycle을 이용해 Rx자원을 해제한다
     */
    override fun onPause(){
        super.onPause()
        when {
            !rxDisposable.isDisposed -> rxDisposable.dispose()
        }
    }
    /*
     * JSON을 파싱한 데이터를 넘겨 받는다
     */
    private fun setUICurrentWeather(data: CurrentWeather?) {
        //현재 날씨정보를 가지고 있는 data객체를 가져온다
        val minutely: Minutely? = data?.weather?.minutely?.get(0)

        /*
         * 현재날씨및 오늘의 최저/최고 온도를 가져온다
         */
        minutely?.let {
            tvTodayCurrentTemperature.text = it.temperature.tc.toDouble().roundToInt().toString()
            maxTv.text = it.temperature.tmax.toDouble().roundToInt().toString()
            minTv.text = it.temperature.tmin.toDouble().roundToInt().toString()

            /*
             * 현재 하늘상태(sky code)맞는 Icon으로
             * Background Image및 Weather Icon 을 세팅한다
             */
            val pair: Pair<Int, Int> = WeatherUtil.currentABGIconCondition(it.sky.code)
            mainRootview.setBackgroundResource(pair.first!!)
            ivCurrentWeatherIcon.setImageResource(pair.second!!)
        }
    }

    /*
     * 미세먼지 원형 바에 표현
     */
    private fun setUIDust(data: CurrentWeather?) {

        //미세먼지 값
        val dustValue = data?.weather?.dust?.get(0)!!.pm10.value

        /*
         * Circular위젯에 표현할 값과 메세지를 쌍으로 얻어온다
         */
        val pair = WeatherUtil.getDustMessage(dustValue.trim())

        /*
         * dustCircular,tvDustGrade는 activity_main
         * 에 선언된 Widget ID 이름
         */
        dustCircular.progressValue = pair.first!!.toFloat()

        tvDustGrade.text = pair.second
    }

    /*
     * 자외선 지수를 원형 바에 표현
     */
    private fun setUIUvindex(data: CurrentWeather?) {
        //자외선 지수
        var uvValue = data?.weather?.wIndex?.uvindex!!.get(0).day00.index
        /*
         * 18시 이후엔 json값이 empty가 된다
         */
        if (uvValue.isNullOrEmpty()) {
            uvValue = "0"
        }
        /*
         * Circular위젯에 표현할 값과 메세지를 쌍으로 얻어온다
         */
        val pair = WeatherUtil.getUvrayMessage(uvValue)

        /*
         * dustCircular,tvDustGrade는 activity_main
         * 에 선언된 Widget ID 이름
         */
        uvCircular.progressValue = pair.first!!.toFloat()
        tvUvStateMessage.text = pair.second
    }
    private fun errorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
