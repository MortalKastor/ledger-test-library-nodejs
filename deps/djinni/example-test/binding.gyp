{
  'targets': [
      {
      'target_name': 'libtestapp_nodejs',
      'sources': [
            "libtestapp.cpp",
      		"<!@(python glob.py generated-src/nodejs    '*.cpp' '*.hpp')",
      		"<!@(python glob.py generated-src/cpp       '*.cpp' '*.hpp')",
      		"<!@(python glob.py handwritten-src/cpp     '*.cpp' '*.hpp')",
       ],
       'include_dirs': [
			"<!(node -e \"require('nan')\")"
        ],
		  'all_dependent_settings': {
			'include_dirs': [
				"<!(node -e \"require('nan')\")"
			],
		},
      'conditions': [
        [ 'OS=="mac"', {
              'LDFLAGS': [
            '-framework IOKit',
            '-framework CoreFoundation'
          ],
          'xcode_settings': {
            'GCC_ENABLE_CPP_EXCEPTIONS': 'YES',
            'OTHER_CFLAGS': [
              '-ObjC++',
              '-std=c++14',
            ],
            'OTHER_LDFLAGS': [
              '-framework IOKit',
              '-framework CoreFoundation'
            ],
          },
        }],
        [ 'OS=="win"', {
			'GCC_ENABLE_CPP_EXCEPTIONS': 'YES',
			'OTHER_CFLAGS': [
			  '-std=c++14',
			]
       	 }]
      ],
      'cflags!': ['-ansi', '-fno-exceptions' ],
      'cflags_cc!': [ '-fno-exceptions' ],
      'cflags': ['-g', '-exceptions'],
      'cflags_cc': ['-g', '-exceptions']
    }
  ]
}
