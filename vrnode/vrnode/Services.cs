// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: vrch/services.proto
#pragma warning disable 1591, 0612, 3021
#region Designer generated code

using pb = global::Google.Protobuf;
using pbc = global::Google.Protobuf.Collections;
using pbr = global::Google.Protobuf.Reflection;
using scg = global::System.Collections.Generic;
namespace Vrch {

  /// <summary>Holder for reflection information generated from vrch/services.proto</summary>
  public static partial class ServicesReflection {

    #region Descriptor
    /// <summary>File descriptor for vrch/services.proto</summary>
    public static pbr::FileDescriptor Descriptor {
      get { return descriptor; }
    }
    private static pbr::FileDescriptor descriptor;

    static ServicesReflection() {
      byte[] descriptorData = global::System.Convert.FromBase64String(
          string.Concat(
            "ChN2cmNoL3NlcnZpY2VzLnByb3RvEgR2cmNoGhN2cmNoL2RpYWxvZ3VlLnBy",
            "b3RvGg92cmNoL3RleHQucHJvdG8aEHZyY2gvdm9pY2UucHJvdG8iOAoISW5j",
            "b21pbmcSGgoFdm9pY2UYASABKAsyCy52cmNoLlZvaWNlEhAKCGtlZXBsaXZl",
            "GAIgASgDIjcKCE91dGdvaW5nEhgKBHRleHQYASABKAsyCi52cmNoLlRleHQS",
            "EQoJa2VlcGFsaXZlGAIgASgDIisKB1JlcXVlc3QSIAoIZGlhbG9ndWUYASAB",
            "KAsyDi52cmNoLkRpYWxvZ3VlIkgKCFJlc3BvbnNlEiAKCGRpYWxvZ3VlGAEg",
            "ASgLMg4udnJjaC5EaWFsb2d1ZRIaCgV2b2ljZRgCIAEoCzILLnZyY2guVm9p",
            "Y2UyQAoQVnJDbHVzdGVyU2VydmljZRIsCgRKb2luEg4udnJjaC5JbmNvbWlu",
            "ZxoOLnZyY2guT3V0Z29pbmciACgBMAEyNgoLVnJjaFNlcnZpY2USJwoEVGFs",
            "axINLnZyY2guUmVxdWVzdBoOLnZyY2guUmVzcG9uc2UiADIuCglWclNlcnZp",
            "Y2USIQoEVGFsaxIKLnZyY2guVGV4dBoLLnZyY2guVm9pY2UiADI1CglDaFNl",
            "cnZpY2USKAoEVGFsaxIOLnZyY2guRGlhbG9ndWUaDi52cmNoLkRpYWxvZ3Vl",
            "IgBiBnByb3RvMw=="));
      descriptor = pbr::FileDescriptor.FromGeneratedCode(descriptorData,
          new pbr::FileDescriptor[] { global::Vrch.DialogueReflection.Descriptor, global::Vrch.TextReflection.Descriptor, global::Vrch.VoiceReflection.Descriptor, },
          new pbr::GeneratedClrTypeInfo(null, new pbr::GeneratedClrTypeInfo[] {
            new pbr::GeneratedClrTypeInfo(typeof(global::Vrch.Incoming), global::Vrch.Incoming.Parser, new[]{ "Voice", "Keeplive" }, null, null, null),
            new pbr::GeneratedClrTypeInfo(typeof(global::Vrch.Outgoing), global::Vrch.Outgoing.Parser, new[]{ "Text", "Keepalive" }, null, null, null),
            new pbr::GeneratedClrTypeInfo(typeof(global::Vrch.Request), global::Vrch.Request.Parser, new[]{ "Dialogue" }, null, null, null),
            new pbr::GeneratedClrTypeInfo(typeof(global::Vrch.Response), global::Vrch.Response.Parser, new[]{ "Dialogue", "Voice" }, null, null, null)
          }));
    }
    #endregion

  }
  #region Messages
  public sealed partial class Incoming : pb::IMessage<Incoming> {
    private static readonly pb::MessageParser<Incoming> _parser = new pb::MessageParser<Incoming>(() => new Incoming());
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public static pb::MessageParser<Incoming> Parser { get { return _parser; } }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public static pbr::MessageDescriptor Descriptor {
      get { return global::Vrch.ServicesReflection.Descriptor.MessageTypes[0]; }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    pbr::MessageDescriptor pb::IMessage.Descriptor {
      get { return Descriptor; }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Incoming() {
      OnConstruction();
    }

    partial void OnConstruction();

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Incoming(Incoming other) : this() {
      Voice = other.voice_ != null ? other.Voice.Clone() : null;
      keeplive_ = other.keeplive_;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Incoming Clone() {
      return new Incoming(this);
    }

    /// <summary>Field number for the "voice" field.</summary>
    public const int VoiceFieldNumber = 1;
    private global::Vrch.Voice voice_;
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public global::Vrch.Voice Voice {
      get { return voice_; }
      set {
        voice_ = value;
      }
    }

    /// <summary>Field number for the "keeplive" field.</summary>
    public const int KeepliveFieldNumber = 2;
    private long keeplive_;
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public long Keeplive {
      get { return keeplive_; }
      set {
        keeplive_ = value;
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override bool Equals(object other) {
      return Equals(other as Incoming);
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public bool Equals(Incoming other) {
      if (ReferenceEquals(other, null)) {
        return false;
      }
      if (ReferenceEquals(other, this)) {
        return true;
      }
      if (!object.Equals(Voice, other.Voice)) return false;
      if (Keeplive != other.Keeplive) return false;
      return true;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override int GetHashCode() {
      int hash = 1;
      if (voice_ != null) hash ^= Voice.GetHashCode();
      if (Keeplive != 0L) hash ^= Keeplive.GetHashCode();
      return hash;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override string ToString() {
      return pb::JsonFormatter.ToDiagnosticString(this);
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void WriteTo(pb::CodedOutputStream output) {
      if (voice_ != null) {
        output.WriteRawTag(10);
        output.WriteMessage(Voice);
      }
      if (Keeplive != 0L) {
        output.WriteRawTag(16);
        output.WriteInt64(Keeplive);
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public int CalculateSize() {
      int size = 0;
      if (voice_ != null) {
        size += 1 + pb::CodedOutputStream.ComputeMessageSize(Voice);
      }
      if (Keeplive != 0L) {
        size += 1 + pb::CodedOutputStream.ComputeInt64Size(Keeplive);
      }
      return size;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void MergeFrom(Incoming other) {
      if (other == null) {
        return;
      }
      if (other.voice_ != null) {
        if (voice_ == null) {
          voice_ = new global::Vrch.Voice();
        }
        Voice.MergeFrom(other.Voice);
      }
      if (other.Keeplive != 0L) {
        Keeplive = other.Keeplive;
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void MergeFrom(pb::CodedInputStream input) {
      uint tag;
      while ((tag = input.ReadTag()) != 0) {
        switch(tag) {
          default:
            input.SkipLastField();
            break;
          case 10: {
            if (voice_ == null) {
              voice_ = new global::Vrch.Voice();
            }
            input.ReadMessage(voice_);
            break;
          }
          case 16: {
            Keeplive = input.ReadInt64();
            break;
          }
        }
      }
    }

  }

  public sealed partial class Outgoing : pb::IMessage<Outgoing> {
    private static readonly pb::MessageParser<Outgoing> _parser = new pb::MessageParser<Outgoing>(() => new Outgoing());
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public static pb::MessageParser<Outgoing> Parser { get { return _parser; } }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public static pbr::MessageDescriptor Descriptor {
      get { return global::Vrch.ServicesReflection.Descriptor.MessageTypes[1]; }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    pbr::MessageDescriptor pb::IMessage.Descriptor {
      get { return Descriptor; }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Outgoing() {
      OnConstruction();
    }

    partial void OnConstruction();

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Outgoing(Outgoing other) : this() {
      Text = other.text_ != null ? other.Text.Clone() : null;
      keepalive_ = other.keepalive_;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Outgoing Clone() {
      return new Outgoing(this);
    }

    /// <summary>Field number for the "text" field.</summary>
    public const int TextFieldNumber = 1;
    private global::Vrch.Text text_;
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public global::Vrch.Text Text {
      get { return text_; }
      set {
        text_ = value;
      }
    }

    /// <summary>Field number for the "keepalive" field.</summary>
    public const int KeepaliveFieldNumber = 2;
    private long keepalive_;
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public long Keepalive {
      get { return keepalive_; }
      set {
        keepalive_ = value;
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override bool Equals(object other) {
      return Equals(other as Outgoing);
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public bool Equals(Outgoing other) {
      if (ReferenceEquals(other, null)) {
        return false;
      }
      if (ReferenceEquals(other, this)) {
        return true;
      }
      if (!object.Equals(Text, other.Text)) return false;
      if (Keepalive != other.Keepalive) return false;
      return true;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override int GetHashCode() {
      int hash = 1;
      if (text_ != null) hash ^= Text.GetHashCode();
      if (Keepalive != 0L) hash ^= Keepalive.GetHashCode();
      return hash;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override string ToString() {
      return pb::JsonFormatter.ToDiagnosticString(this);
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void WriteTo(pb::CodedOutputStream output) {
      if (text_ != null) {
        output.WriteRawTag(10);
        output.WriteMessage(Text);
      }
      if (Keepalive != 0L) {
        output.WriteRawTag(16);
        output.WriteInt64(Keepalive);
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public int CalculateSize() {
      int size = 0;
      if (text_ != null) {
        size += 1 + pb::CodedOutputStream.ComputeMessageSize(Text);
      }
      if (Keepalive != 0L) {
        size += 1 + pb::CodedOutputStream.ComputeInt64Size(Keepalive);
      }
      return size;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void MergeFrom(Outgoing other) {
      if (other == null) {
        return;
      }
      if (other.text_ != null) {
        if (text_ == null) {
          text_ = new global::Vrch.Text();
        }
        Text.MergeFrom(other.Text);
      }
      if (other.Keepalive != 0L) {
        Keepalive = other.Keepalive;
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void MergeFrom(pb::CodedInputStream input) {
      uint tag;
      while ((tag = input.ReadTag()) != 0) {
        switch(tag) {
          default:
            input.SkipLastField();
            break;
          case 10: {
            if (text_ == null) {
              text_ = new global::Vrch.Text();
            }
            input.ReadMessage(text_);
            break;
          }
          case 16: {
            Keepalive = input.ReadInt64();
            break;
          }
        }
      }
    }

  }

  public sealed partial class Request : pb::IMessage<Request> {
    private static readonly pb::MessageParser<Request> _parser = new pb::MessageParser<Request>(() => new Request());
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public static pb::MessageParser<Request> Parser { get { return _parser; } }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public static pbr::MessageDescriptor Descriptor {
      get { return global::Vrch.ServicesReflection.Descriptor.MessageTypes[2]; }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    pbr::MessageDescriptor pb::IMessage.Descriptor {
      get { return Descriptor; }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Request() {
      OnConstruction();
    }

    partial void OnConstruction();

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Request(Request other) : this() {
      Dialogue = other.dialogue_ != null ? other.Dialogue.Clone() : null;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Request Clone() {
      return new Request(this);
    }

    /// <summary>Field number for the "dialogue" field.</summary>
    public const int DialogueFieldNumber = 1;
    private global::Vrch.Dialogue dialogue_;
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public global::Vrch.Dialogue Dialogue {
      get { return dialogue_; }
      set {
        dialogue_ = value;
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override bool Equals(object other) {
      return Equals(other as Request);
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public bool Equals(Request other) {
      if (ReferenceEquals(other, null)) {
        return false;
      }
      if (ReferenceEquals(other, this)) {
        return true;
      }
      if (!object.Equals(Dialogue, other.Dialogue)) return false;
      return true;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override int GetHashCode() {
      int hash = 1;
      if (dialogue_ != null) hash ^= Dialogue.GetHashCode();
      return hash;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override string ToString() {
      return pb::JsonFormatter.ToDiagnosticString(this);
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void WriteTo(pb::CodedOutputStream output) {
      if (dialogue_ != null) {
        output.WriteRawTag(10);
        output.WriteMessage(Dialogue);
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public int CalculateSize() {
      int size = 0;
      if (dialogue_ != null) {
        size += 1 + pb::CodedOutputStream.ComputeMessageSize(Dialogue);
      }
      return size;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void MergeFrom(Request other) {
      if (other == null) {
        return;
      }
      if (other.dialogue_ != null) {
        if (dialogue_ == null) {
          dialogue_ = new global::Vrch.Dialogue();
        }
        Dialogue.MergeFrom(other.Dialogue);
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void MergeFrom(pb::CodedInputStream input) {
      uint tag;
      while ((tag = input.ReadTag()) != 0) {
        switch(tag) {
          default:
            input.SkipLastField();
            break;
          case 10: {
            if (dialogue_ == null) {
              dialogue_ = new global::Vrch.Dialogue();
            }
            input.ReadMessage(dialogue_);
            break;
          }
        }
      }
    }

  }

  public sealed partial class Response : pb::IMessage<Response> {
    private static readonly pb::MessageParser<Response> _parser = new pb::MessageParser<Response>(() => new Response());
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public static pb::MessageParser<Response> Parser { get { return _parser; } }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public static pbr::MessageDescriptor Descriptor {
      get { return global::Vrch.ServicesReflection.Descriptor.MessageTypes[3]; }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    pbr::MessageDescriptor pb::IMessage.Descriptor {
      get { return Descriptor; }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Response() {
      OnConstruction();
    }

    partial void OnConstruction();

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Response(Response other) : this() {
      Dialogue = other.dialogue_ != null ? other.Dialogue.Clone() : null;
      Voice = other.voice_ != null ? other.Voice.Clone() : null;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public Response Clone() {
      return new Response(this);
    }

    /// <summary>Field number for the "dialogue" field.</summary>
    public const int DialogueFieldNumber = 1;
    private global::Vrch.Dialogue dialogue_;
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public global::Vrch.Dialogue Dialogue {
      get { return dialogue_; }
      set {
        dialogue_ = value;
      }
    }

    /// <summary>Field number for the "voice" field.</summary>
    public const int VoiceFieldNumber = 2;
    private global::Vrch.Voice voice_;
    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public global::Vrch.Voice Voice {
      get { return voice_; }
      set {
        voice_ = value;
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override bool Equals(object other) {
      return Equals(other as Response);
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public bool Equals(Response other) {
      if (ReferenceEquals(other, null)) {
        return false;
      }
      if (ReferenceEquals(other, this)) {
        return true;
      }
      if (!object.Equals(Dialogue, other.Dialogue)) return false;
      if (!object.Equals(Voice, other.Voice)) return false;
      return true;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override int GetHashCode() {
      int hash = 1;
      if (dialogue_ != null) hash ^= Dialogue.GetHashCode();
      if (voice_ != null) hash ^= Voice.GetHashCode();
      return hash;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public override string ToString() {
      return pb::JsonFormatter.ToDiagnosticString(this);
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void WriteTo(pb::CodedOutputStream output) {
      if (dialogue_ != null) {
        output.WriteRawTag(10);
        output.WriteMessage(Dialogue);
      }
      if (voice_ != null) {
        output.WriteRawTag(18);
        output.WriteMessage(Voice);
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public int CalculateSize() {
      int size = 0;
      if (dialogue_ != null) {
        size += 1 + pb::CodedOutputStream.ComputeMessageSize(Dialogue);
      }
      if (voice_ != null) {
        size += 1 + pb::CodedOutputStream.ComputeMessageSize(Voice);
      }
      return size;
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void MergeFrom(Response other) {
      if (other == null) {
        return;
      }
      if (other.dialogue_ != null) {
        if (dialogue_ == null) {
          dialogue_ = new global::Vrch.Dialogue();
        }
        Dialogue.MergeFrom(other.Dialogue);
      }
      if (other.voice_ != null) {
        if (voice_ == null) {
          voice_ = new global::Vrch.Voice();
        }
        Voice.MergeFrom(other.Voice);
      }
    }

    [global::System.Diagnostics.DebuggerNonUserCodeAttribute]
    public void MergeFrom(pb::CodedInputStream input) {
      uint tag;
      while ((tag = input.ReadTag()) != 0) {
        switch(tag) {
          default:
            input.SkipLastField();
            break;
          case 10: {
            if (dialogue_ == null) {
              dialogue_ = new global::Vrch.Dialogue();
            }
            input.ReadMessage(dialogue_);
            break;
          }
          case 18: {
            if (voice_ == null) {
              voice_ = new global::Vrch.Voice();
            }
            input.ReadMessage(voice_);
            break;
          }
        }
      }
    }

  }

  #endregion

}

#endregion Designer generated code
