{
  'targets': [
    {
      'target_name': 'libledgerapp',
      'type': 'static_library',
      'conditions': [],
      'dependencies': [
        'deps/json11.gyp:json11'
      ],
      'sources': [
        # just automatically include all cpp and hpp files in src/ (for now)
        # '<!' is shell expand
        # '@' is to splat the arguments into list items
        "<!@(python glob.py src/ *.cpp *.hpp)",
      ],
      'include_dirs': [
        'include',
      ],
      'all_dependent_settings': {
        'include_dirs': [
          'include',
          'deps',
        ],
      },
    },
    {
      'target_name': 'libledgerapp_objc',
      'type': 'static_library',
      'conditions': [],
      'dependencies': [
        'deps/djinni/support-lib/support_lib.gyp:djinni_objc',
        'libledgerapp',
      ],
      'sources': [
        '<!@(python glob.py objc *.mm *.h *.m)',
      ],
      'sources!': ['play.m'],
      'include_dirs': [
        'include',
        'objc',
      ],
      'all_dependent_settings': {
        'include_dirs': [
          'include',
          'objc',
        ],
      },
    },
    {
      'target_name': 'libledgerapp_android',
      'android_unmangled_name': 1,
      'type': 'shared_library',
      'dependencies': [
        'deps/djinni/support-lib/support_lib.gyp:djinni_jni',
        'libledgerapp',
      ],
      'ldflags' : [ '-llog' ],
      'sources': [
        '<!@(python glob.py android/jni *.cpp *.hpp)',
        '<!@(python glob.py android/jni_gen *.cpp *.hpp)',
      ],
      'include_dirs': [
        'include',
        'src/interface',
      ],
      'all_dependent_settings': {
        'include_dirs': [
          'include',
          'src/interface',
        ],
      },
    },
    {
      'target_name': 'play_objc',
      'type': 'executable',
      'dependencies': ['libledgerapp_objc'],
      'libraries': [
        'libc++.a',
      ],
      'sources': [
        'objc/play.m',
      ],
    }
  ],
}
