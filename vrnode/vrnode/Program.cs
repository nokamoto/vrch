using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using Grpc.Core;

namespace vrnode
{
    class Program
    {
        static async Task Join(Channel channel, VrController ctl, Vrch.VrClusterService.VrClusterServiceClient client, string directory, string apikey)
        {
            long filename = 0;
            Metadata metadata = new Metadata();
            metadata.Add("x-api-key", apikey);

            using (var call = client.Join(headers: metadata))
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
                            Interlocked.Exchange(ref ack, text.Keepalive);
                            Console.WriteLine("pong: " + text.Keepalive);
                        }
                    }
                });

                long keepalive = 0;

                while (true)
                {
                    
                    Thread.Sleep(10 * 1000);

                    if (keepalive != Interlocked.Read(ref ack))
                    {
                        throw new InvalidOperationException("ping/pong failed.");
                    }

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
            string apikey = args[3];

            VrController ctl = new VrController(150, 50);

            Console.WriteLine(string.Format("host={0}, port={1}, wave files directory={2}, apikey={3}", host, port, directory, apikey));

            Channel channel = new Channel(string.Format("{0}:{1}", host, port), ChannelCredentials.Insecure);

            while (true)
            {
                try
                {
                    var client = new Vrch.VrClusterService.VrClusterServiceClient(channel);

                    var task = Join(channel, ctl, client, directory, apikey);

                    task.Wait();
                } catch (Exception e)
                {
                    Console.WriteLine(e.ToString());

                    Console.WriteLine("sleep 10 seconds...");

                    Thread.Sleep(10 * 1000);
                }
            }
        }
    }
}
