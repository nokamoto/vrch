# vrch

[![Build Status](https://travis-ci.org/nokamoto/vrch.svg?branch=master)](https://travis-ci.org/nokamoto/vrch)
[![CircleCI](https://circleci.com/gh/nokamoto/vrch/tree/master.svg?style=svg)](https://circleci.com/gh/nokamoto/vrch/tree/master)

![Overview](https://user-images.githubusercontent.com/4374383/28465840-2b129796-6e65-11e7-99ac-87be6aaec9ef.png)

- [gRPC Server](https://github.com/nokamoto/vrch#vrchgrpc)
- [Windows Node](https://github.com/nokamoto/vrch#vrnode)
- [Slack App](https://github.com/nokamoto/vrch#slackbridge)
- [Android App](https://github.com/nokamoto/vrch#vrchandroid)

## vrchgrpc

### Getting Started on GCP
[Getting Started with gRPC on Container Engine](https://cloud.google.com/endpoints/docs/get-started-grpc-container-engine)

手順に従いGCPの設定を行います。

以下必要に応じて手順簡略化のためのスクリプトを利用します。

#### Deploying the Endpoints Configuration
```
$ ./gcp/sed-api_config.sh [PROJECT_ID]
$ ./gcp/setup-api-config.sh

Service Configuration [SERVICE_CONFIG_ID] uploaded for service [SERVICE_NAME]

```

#### Deploying the sample API and ESP to the cluster
[雑談対話API](https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_name=dialogue&p_name=api_reference) の `[DOCOMO_API_KEY]` を取得します。

```
$ ./gcp/sed-grpc-k8s-tempalte.sh [SERVICE_NAME] [SERVICE_CONFIG_ID] [DOCOMO_API_KEY]
$ kubectl create -f gcp/grpc-k8s.yaml
```


```
$ kubectl get service // [EXTERNAL_IP]
```

##### Configuring Endpoints DNS
必要であれば [Configuring Endpoints DNS](https://cloud.google.com/endpoints/docs/grpc-dns-configure) で DNS を設定します。

```
$ ./gcp/sed-api_config.sh [PROJECT_ID] [EXTERNAL_IP]
$ ./gcp/setup-api-config.sh

$ dig vrch.endpoints.[PROJECT_ID].cloud.goog +short
[EXTERNAL_IP]
```

DNS 設定後は `[EXTERNAL_IP]` は FQDN を利用できます。

## vrnode

Voiceroid を起動した状態で vrnode を起動します。（東北きりたんのみ動作確認済み）

[Restricting API Access with API Keys (gRPC)](https://cloud.google.com/endpoints/docs/restricting-api-access-with-api-keys-grpc) の `[GCP_API_KEY]` を取得します。

```
vrnode.exe [EXTERNAL_IP] 80 [Voiceroid Wave Files Directory] [GCP_API_KEY]
```

## slackbridge

Slack 上での会話を行うために [Bot Users](https://api.slack.com/bot-users) を作成し起動します。

[Add the Firebase Admin SDK to Your Server](https://firebase.google.com/docs/admin/setup) で `firebase-adminsdk.json` と `https://<DATABASE_NAME>.firebaseio.com/` を取得します。

[Firebase Console](https://console.firebase.google.com) で `gs://<BUCKET>` を取得します。

```
docker run\
    -e "SLACK_TOKEN=[SLACK_TOKEN]"\
    -e "GRPC_HOST=[EXTERNAL_IP]"\
    -e "SLACK_CHANNEL=[SLACK_CHANNEL]"\
    -e "GCP_API_KEY=[GCP_API_KEY]"\
    -e "FIREBASE_ADMIN_SDK_JSON=[FIREBASE_ADMIN_SDK_JSON]"\
    -e "FIREBASE_ADMIN_SDK_URL=[FIREBASE_ADMIN_SDK_URL]"\
    -e "FIREBASE_STORAGE_BUCKET=[FIREBASE_STORAGE_BUCKET]"\
    -v [/path/to/firebase-adminsdk.json]:[FIREBASE_ADMIN_SDK_JSON]\
    -d nokamotohub/slackbridge
```

チャンネル上の発言に反応して音声がアップロードされます。

![slack thumbnail](https://user-images.githubusercontent.com/4374383/27837403-fafec41a-611e-11e7-978f-76bdadf064ba.png)

## VrchAndroid
https://play.google.com/apps/testing/nokamoto.github.com.vrchandroid

beta公開のため以下のグループに入る必要があります。
https://groups.google.com/forum/#!forum/nokamoto
