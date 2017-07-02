﻿using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using Grpc.Core;

namespace vrnode
{
    class Program
    {
        static async Task Join(Channel channel, VrController ctl, Vrch.VrClusterService.VrClusterServiceClient client, string directory)
        {
            long filename = 0;
            using (var call = client.Join())
            {
                long ack = 0;

                var task = Task.Run(async () =>
                {
                    while (await call.ResponseStream.MoveNext())
                    {
                        var text = call.ResponseStream.Current;

                        Console.WriteLine("Received" + text);

                        if (text.Keepalive.Equals(0))
                        {
                            if (text.Text.Text_.Length == 0)
                            {
                                Console.WriteLine("ignore empty talk.");
                            }
                            else
                            {
                                filename = filename + 1;
                                var bytes = ctl.Talk(text.Text.Text_, directory, "vrnode" + (filename % 10));

                                await call.RequestStream.WriteAsync(new Vrch.Incoming { Voice = new Vrch.Voice { Voice_ = Google.Protobuf.ByteString.CopyFrom(bytes) } });
                            }
                        }
                        else
                        {
                            ack = text.Keepalive;
                            Console.WriteLine("pong: " + text.Keepalive);
                        }
                    }
                });

                long keepalive = 0;

                while (true)
                {
                    Thread.Sleep(30 * 1000);
                    //if (keepalive != ack)
                    //{
                    //    throw new InvalidOperationException("ping/pong failed.");
                    //}
                    Console.WriteLine("ping: " + (keepalive + 1));
                    call.RequestStream.WriteAsync(new Vrch.Incoming { Keeplive = ++keepalive });
                }
            }
        }

        static void Main(string[] args)
        {
            string host = args[0];
            int port = int.Parse(args[1]);
            string directory = args[2];

            Console.WriteLine(string.Format("host={0}, port={1}, wave files directory={2}", host, port, directory));

            Channel channel = new Channel(string.Format("{0}:{1}", host, port), ChannelCredentials.Insecure);
            VrController ctl = new VrController(100, 50);

            var client = new Vrch.VrClusterService.VrClusterServiceClient(channel);

            var task = Join(channel, ctl, client, directory);

            task.Wait();
             
            channel.ShutdownAsync().Wait();
        }
    }
}
