# vrch

## vrchgrpc

### Getting Started on GCP
[Getting Started with gRPC on Container Engine](https://cloud.google.com/endpoints/docs/get-started-grpc-container-engine)

手順に従いGCPの設定を行います。

以下必要に応じて手順簡略化のためのスクリプトを利用します。

#### Deploying the Endpoints Configuration
```
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
$ kubectl get service // [SERVER_IP]
```

## vrnode

Voiceroid を起動した状態で vrnode を起動します。（東北きりたんのみ動作確認済み）

[Restricting API Access with API Keys (gRPC)](https://cloud.google.com/endpoints/docs/restricting-api-access-with-api-keys-grpc) の `[GCP_API_KEY]` を取得します。

```
vrnode.exe [SERVER_IP] 80 [Voiceroid Wave Files Directory] [GCP_API_KEY]
```

## slackbridge

Slack 上での会話を行うために [Bot Users](https://api.slack.com/bot-users) を作成し起動します。

```
docker run -e "SLACK_TOKEN=[SLACK_TOKEN]" -e "VRCH_HOST=[SERVER_IP]" -e "SLACK_CHANNEL=[SLACK_CHANNEL]" -e "GCP_API_KEY=[GCP_API_KEY]" -d nokamotohub/slackbridge
```

チャンネル上の発言に反応して音声がアップロードされます。

![slack thumbnail](https://user-images.githubusercontent.com/4374383/27837403-fafec41a-611e-11e7-978f-76bdadf064ba.png)
