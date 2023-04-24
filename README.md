# RhiLa

AWSのLambdaは、古くはAPI-Gateway経由、最近サポートされた関数URLによって、Webアプリが作れる環境が提供されている。

しかし、この場合コールドスタート問題があり、起動までに「でかいライブラリを読めば読むほど起動時間がとてもかかる」結果となる。

なので、[LFU](https://github.com/maachang/LFU) では、これらの問題である、でかいライブラリ（たとえばaws.sdkV2)などを読むと、それを読み込むだけでコールドスタート時に大体６秒とかの時間を要するものを、AWS APIアクセスを実装することで、短い時間で接続可能して、それによって「普通に安価にWebアプリ」を作る事を目指している。

しかし、これでも「コールドスタート時に遅い場合は１～２秒」近くかかるので、コールドスタート時にサクサクなWebアプリは期待できないわけで、まあ実用に耐えれるけど「もう安定的に早く動いてほしい」ってのがある。

と言うのも、このLFUを作成している最中の去年の年末頃に、snapStart機能がAWSのLambdaに機能追加された。

https://aws.amazon.com/jp/blogs/news/reducing-java-cold-starts-on-aws-lambda-functions-with-snapstart/

これは何かと言えば、Java11環境に現在限定されている(2023/02/17時点)が、この機能によってJavaのスタートアップ実行部分をSnapShot化して、次からその情報を読まない事で、最大コールドスタート時において１０倍の高速起動ができると言う話がある。

ただ、起動時に初期化処理を行う的なものに関しては、SnapShotされたものが動くので毎回同じ情報となり、たとえば「乱数発生プログラム」などは、初期化の場合はSnapShotで毎回同じ値を拾うなんて事になりかねないわけで、その辺使い勝手が悪い。

あと「javaで動く」ので当然「メモリ容量」もたくさん必要で、その結果「高いコスト」での対応となるので、メリットは十分あるが、これよりもっと効率のよいものが無いかと思い調べて見た次第。

https://dev.classmethod.jp/articles/measure-container-image-lambda-coldstart/

１つは、Dockerを使ってLambdaで実行する事ができると言う事。

つまり、たとえばC言語で作った実行プログラムなら、オーバーヘッドが最小で起動ができると言う事。

ただ、現実的にC言語等でWebアプリを作るにはコストがかかりすぎるので、他の選択になるかと思います。

そこで考えられるのが、２つの構成

1. graalVM(java)を利用したNative化
2. rhinoを使ったJavascirptEngine

これらを踏まえてDockerImage化して、それをAWSLambdaで利用すればコールドスタートに対して、高速に起動することができる。

https://www.slideshare.net/ShinjiTakao/graalvmjava-how-fast-does-graalvms-java-native-build-feature-launch-java-programs-evaluation-on-a-serverless-platform

と言うのもgraalVM(java)を利用したNative化によって、Javaの起動と比べて１００倍ぐらい高速起動（C言語のプログラム並）のものとなる。

また、このNativeImage化の利点として、メモリ利用の改善が生まれるわけで、つまり生Javaの場合は起動時にライブラリをLoadするがこの時に中間ファイル(class)からNativeImage変換するのに、メモリとCPUを利用するが、これが初めからNativeImage化されているので、これらのコストが無くなり、結果としてメモリ利用の改善となる。

あとRhinoは、Javaで実行してもそれなりの速度でJavascriptが実行できるわけで、これを使う事で「速度はV8より遅い」が、コールドスタートの起動速度はNodeJsよりは、早くなるので相対的に結果的に早い安いのコスト実現が行える。

あとLFUの考え方である「実際に動くもの」は「GithubやS3Bucket上のコンテンツを利用」と考え、Lambda環境自身は「Coreで変更が不要なもの」としての立ち位置で「利用する」ものであるから、別に「Docketコンテナ + graalVM=NativeImage + RhinoEngine(js)」の更新頻度が低くても、全く問題は無くなる。

それと、そもそも「LambdaのNoteJSにおける関数URL」のような「サーバレスWebアプリ」に対して `async` これはそれほど重要でないわけで、正に「Legacy」である「RhinoのJsEngine」だと、あらゆる処理が「同期」実行で「行える」ので、コーディングの煩わしさ」も「軽減」される。

因みにRhinoは `async` 等も未サポートのレガシー環境で、Lambda環境のような環境では「これら`async` や `Promise` は不要」なので、同期でコーディングし易い環境が構築できる可能性が高い。

この辺を踏まえて「実験的」な形でAWSLambdaのサーバレス環境を安価で高速に動かせる仕組みを作りたいと思う。