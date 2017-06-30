#!/bin/sh

set -ex

packages/Grpc.Tools.1.4.1/tools/windows_x86/protoc.exe -I../vr/src/main/protobuf --csharp_out vrnode --grpc_out vrnode ../vr/src/main/protobuf/vrch/services.proto --plugin=protoc-gen-grpc=packages/Grpc.Tools.1.4.1/tools/windows_x86/grpc_csharp_plugin.exe

packages/Grpc.Tools.1.4.1/tools/windows_x86/protoc.exe -I../vr/src/main/protobuf --csharp_out vrnode --grpc_out vrnode ../vr/src/main/protobuf/vrch/text.proto --plugin=protoc-gen-grpc=packages/Grpc.Tools.1.4.1/tools/windows_x86/grpc_csharp_plugin.exe

packages/Grpc.Tools.1.4.1/tools/windows_x86/protoc.exe -I../vr/src/main/protobuf --csharp_out vrnode --grpc_out vrnode ../vr/src/main/protobuf/vrch/voice.proto --plugin=protoc-gen-grpc=packages/Grpc.Tools.1.4.1/tools/windows_x86/grpc_csharp_plugin.exe

packages/Grpc.Tools.1.4.1/tools/windows_x86/protoc.exe -I../vr/src/main/protobuf --csharp_out vrnode --grpc_out vrnode ../vr/src/main/protobuf/vrch/dialogue.proto --plugin=protoc-gen-grpc=packages/Grpc.Tools.1.4.1/tools/windows_x86/grpc_csharp_plugin.exe
